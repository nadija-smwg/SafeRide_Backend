import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import '../services/driver_service.dart';
import '../services/student_service.dart';

class LiveTrackingScreen extends StatefulWidget {
  final Student student;
  const LiveTrackingScreen({super.key, required this.student});

  @override
  State<LiveTrackingScreen> createState() => _LiveTrackingScreenState();
}

class _LiveTrackingScreenState extends State<LiveTrackingScreen>
    with TickerProviderStateMixin {
  final DriverService driverService = DriverService();
  final MapController _mapController = MapController();

  LatLng? busPosition;
  List<LatLng> pathPoints = [];
  bool isLoading = true;
  bool hasError = false;
  Timer? _refreshTimer;
  int _refreshCount = 0;

  // Animations
  late AnimationController _busAnim;
  late AnimationController _pulseAnim;
  late Animation<double> _pulseScale;
  late AnimationController _cardSlideAnim;
  late Animation<Offset> _cardSlide;

  // App theme
  static const Color _primary = Color(0xFF00C2E0);
  static const Color _primaryDark = Color(0xFF0099B5);
  static const Color _bg = Color(0xFFF0F6FF);

  @override
  void initState() {
    super.initState();

    _busAnim = AnimationController(
        vsync: this, duration: const Duration(milliseconds: 600));

    _pulseAnim = AnimationController(
        vsync: this, duration: const Duration(milliseconds: 1500))
      ..repeat(reverse: true);
    _pulseScale = Tween<double>(begin: 0.9, end: 1.1).animate(
        CurvedAnimation(parent: _pulseAnim, curve: Curves.easeInOut));

    _cardSlideAnim = AnimationController(
        vsync: this, duration: const Duration(milliseconds: 500));
    _cardSlide = Tween<Offset>(
            begin: const Offset(0, 1), end: Offset.zero)
        .animate(CurvedAnimation(
            parent: _cardSlideAnim, curve: Curves.easeOutCubic));

    _fetchLocation();
    _refreshTimer =
        Timer.periodic(const Duration(seconds: 5), (_) => _fetchLocation());
  }

  @override
  void dispose() {
    _refreshTimer?.cancel();
    _busAnim.dispose();
    _pulseAnim.dispose();
    _cardSlideAnim.dispose();
    super.dispose();
  }

  Future<void> _fetchLocation() async {
    if (widget.student.assignedDriverId == null) {
      if (mounted) setState(() { isLoading = false; hasError = true; });
      return;
    }

    final loc =
        await driverService.getDriverLocation(widget.student.assignedDriverId!);

    if (!mounted) return;

    if (loc != null &&
        loc['latitude'] != null &&
        loc['longitude'] != null) {
      final newPos = LatLng(
        (loc['latitude'] as num).toDouble(),
        (loc['longitude'] as num).toDouble(),
      );

      setState(() {
        busPosition = newPos;
        // Keep a rolling trail of the last 50 positions
        if (pathPoints.isEmpty ||
            pathPoints.last.latitude != newPos.latitude ||
            pathPoints.last.longitude != newPos.longitude) {
          pathPoints.add(newPos);
          if (pathPoints.length > 50) pathPoints.removeAt(0);
        }
        isLoading = false;
        hasError = false;
        _refreshCount++;
      });

      // Smoothly move the map camera to follow the bus
      try {
        _mapController.move(newPos, _mapController.camera.zoom);
      } catch (_) {}

      _busAnim.forward(from: 0);
      if (_refreshCount == 1) {
        _cardSlideAnim.forward();
      }
    } else {
      setState(() {
        isLoading = false;
        hasError = busPosition == null;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _bg,
      body: Stack(
        children: [
          // ─── Map fills entire screen ───────────────────────────────────
          _buildMap(),

          // ─── Top gradient + AppBar ─────────────────────────────────────
          _buildTopBar(context),

          // ─── Status pill (top-right) ───────────────────────────────────
          if (!isLoading && !hasError) _buildStatusPill(),

          // ─── Bottom info card ──────────────────────────────────────────
          if (!isLoading && !hasError && busPosition != null)
            _buildInfoCard(),

          // ─── Loading overlay ───────────────────────────────────────────
          if (isLoading) _buildLoadingOverlay(),

          // ─── Error state ───────────────────────────────────────────────
          if (!isLoading && hasError) _buildErrorState(),
        ],
      ),
    );
  }

  // ════════════════════════════════════════════════════════════════════════
  // MAP
  // ════════════════════════════════════════════════════════════════════════
  Widget _buildMap() {
    const defaultCenter = LatLng(6.9271, 79.8612); // Colombo fallback

    return FlutterMap(
      mapController: _mapController,
      options: MapOptions(
        initialCenter: busPosition ?? defaultCenter,
        initialZoom: 15.5,
        interactionOptions: const InteractionOptions(
          flags: InteractiveFlag.all,
        ),
      ),
      children: [
        // OSM tile layer (FREE — no API key)
        TileLayer(
          urlTemplate: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
          userAgentPackageName: 'com.example.my_app',
          maxZoom: 19,
        ),

        // Bus trail polyline
        if (pathPoints.length > 1)
          PolylineLayer(
            polylines: [
              Polyline(
                points: pathPoints,
                strokeWidth: 5.0,
                color: _primary.withOpacity(0.75),
                borderColor: Colors.white,
                borderStrokeWidth: 1.5,
              ),
            ],
          ),

        // Pulse ring around bus
        if (busPosition != null)
          CircleLayer(
            circles: [
              CircleMarker(
                point: busPosition!,
                radius: 28,
                color: _primary.withOpacity(0.15),
                borderColor: _primary.withOpacity(0.4),
                borderStrokeWidth: 2,
                useRadiusInMeter: false,
              ),
            ],
          ),

        // Bus marker
        if (busPosition != null)
          MarkerLayer(
            markers: [
              Marker(
                point: busPosition!,
                width: 56,
                height: 56,
                child: ScaleTransition(
                  scale: _pulseScale,
                  child: _buildBusMarker(),
                ),
              ),
            ],
          ),

        // Attribution
        RichAttributionWidget(
          attributions: [
            TextSourceAttribution(
              'OpenStreetMap contributors',
              onTap: () {},
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildBusMarker() {
    return Container(
      decoration: BoxDecoration(
        color: _primary,
        shape: BoxShape.circle,
        boxShadow: [
          BoxShadow(
            color: _primary.withOpacity(0.5),
            blurRadius: 12,
            spreadRadius: 2,
          )
        ],
      ),
      child: const Icon(
        Icons.directions_bus_rounded,
        color: Colors.white,
        size: 30,
      ),
    );
  }

  // ════════════════════════════════════════════════════════════════════════
  // TOP BAR
  // ════════════════════════════════════════════════════════════════════════
  Widget _buildTopBar(BuildContext context) {
    return Positioned(
      top: 0,
      left: 0,
      right: 0,
      child: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [
              Colors.white.withOpacity(0.97),
              Colors.white.withOpacity(0.0),
            ],
            stops: const [0.55, 1.0],
          ),
        ),
        child: SafeArea(
          child: Padding(
            padding:
                const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
            child: Row(
              children: [
                // Back button
                Material(
                  color: Colors.white,
                  shape: const CircleBorder(),
                  elevation: 2,
                  child: InkWell(
                    customBorder: const CircleBorder(),
                    onTap: () => Navigator.pop(context),
                    child: const Padding(
                      padding: EdgeInsets.all(10),
                      child: Icon(Icons.arrow_back_ios_new,
                          color: Colors.black87, size: 18),
                    ),
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Live Bus Tracking',
                        style: TextStyle(
                          color: Colors.black87,
                          fontWeight: FontWeight.bold,
                          fontSize: 17,
                        ),
                      ),
                      Text(
                        widget.student.fullName,
                        style: TextStyle(
                          color: Colors.grey.shade600,
                          fontSize: 13,
                        ),
                      ),
                    ],
                  ),
                ),
                // Recenter button
                Material(
                  color: Colors.white,
                  shape: const CircleBorder(),
                  elevation: 2,
                  child: InkWell(
                    customBorder: const CircleBorder(),
                    onTap: () {
                      if (busPosition != null) {
                        _mapController.move(busPosition!, 15.5);
                      }
                    },
                    child: const Padding(
                      padding: EdgeInsets.all(10),
                      child: Icon(Icons.my_location,
                          color: Color(0xFF00C2E0), size: 20),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                // Refresh button
                Material(
                  color: Colors.white,
                  shape: const CircleBorder(),
                  elevation: 2,
                  child: InkWell(
                    customBorder: const CircleBorder(),
                    onTap: _fetchLocation,
                    child: const Padding(
                      padding: EdgeInsets.all(10),
                      child: Icon(Icons.refresh,
                          color: Colors.black54, size: 20),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  // ════════════════════════════════════════════════════════════════════════
  // STATUS PILL
  // ════════════════════════════════════════════════════════════════════════
  Widget _buildStatusPill() {
    return Positioned(
      top: MediaQuery.of(context).padding.top + 68,
      right: 12,
      child: AnimatedBuilder(
        animation: _pulseAnim,
        builder: (_, __) => Transform.scale(
          scale: 0.97 + 0.03 * _pulseAnim.value,
          child: Container(
            padding:
                const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(20),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.1),
                  blurRadius: 8,
                  offset: const Offset(0, 2),
                )
              ],
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(
                  width: 8,
                  height: 8,
                  decoration: const BoxDecoration(
                    shape: BoxShape.circle,
                    color: Colors.green,
                  ),
                ),
                const SizedBox(width: 6),
                const Text(
                  'Live',
                  style: TextStyle(
                    color: Colors.green,
                    fontWeight: FontWeight.bold,
                    fontSize: 12,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  // ════════════════════════════════════════════════════════════════════════
  // BOTTOM INFO CARD
  // ════════════════════════════════════════════════════════════════════════
  Widget _buildInfoCard() {
    return Positioned(
      bottom: 0,
      left: 0,
      right: 0,
      child: SlideTransition(
        position: _cardSlide,
        child: Container(
          decoration: const BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
            boxShadow: [
              BoxShadow(
                color: Colors.black12,
                blurRadius: 20,
                offset: Offset(0, -4),
              )
            ],
          ),
          padding: const EdgeInsets.fromLTRB(20, 14, 20, 28),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Drag handle
              Container(
                width: 40,
                height: 4,
                margin: const EdgeInsets.only(bottom: 14),
                decoration: BoxDecoration(
                  color: Colors.grey.shade300,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),

              // Status row
              Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      gradient: const LinearGradient(
                        colors: [_primary, _primaryDark],
                        begin: Alignment.topLeft,
                        end: Alignment.bottomRight,
                      ),
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: const Icon(Icons.directions_bus_rounded,
                        color: Colors.white, size: 28),
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          'Bus is En Route',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            fontSize: 16,
                            color: Colors.black87,
                          ),
                        ),
                        const SizedBox(height: 3),
                        Text(
                          '${widget.student.fullName} is on the school van',
                          style: TextStyle(
                              color: Colors.grey.shade600, fontSize: 13),
                        ),
                      ],
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 16),
              const Divider(height: 1),
              const SizedBox(height: 14),

              // Coordinate info
              Row(
                children: [
                  Expanded(
                    child: _infoTile(
                      icon: Icons.location_on_rounded,
                      label: 'Latitude',
                      value: busPosition!.latitude.toStringAsFixed(5),
                      color: Colors.redAccent,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _infoTile(
                      icon: Icons.explore_rounded,
                      label: 'Longitude',
                      value: busPosition!.longitude.toStringAsFixed(5),
                      color: Colors.blueAccent,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _infoTile(
                      icon: Icons.timeline_rounded,
                      label: 'Trail Points',
                      value: '${pathPoints.length}',
                      color: Colors.purple,
                    ),
                  ),
                ],
              ),

              const SizedBox(height: 14),

              // Refreshes every 5s indicator
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                decoration: BoxDecoration(
                  color: Colors.green.shade50,
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Icon(Icons.sync, color: Colors.green.shade600, size: 16),
                    const SizedBox(width: 6),
                    Text(
                      'Auto-updating every 5 seconds',
                      style: TextStyle(
                          color: Colors.green.shade700,
                          fontSize: 12,
                          fontWeight: FontWeight.w500),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _infoTile({
    required IconData icon,
    required String label,
    required String value,
    required Color color,
  }) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 10),
      decoration: BoxDecoration(
        color: color.withOpacity(0.07),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, size: 16, color: color),
          const SizedBox(height: 4),
          Text(label,
              style: TextStyle(
                  color: Colors.grey.shade500,
                  fontSize: 10,
                  fontWeight: FontWeight.w500)),
          const SizedBox(height: 2),
          Text(value,
              style: TextStyle(
                  color: Colors.black87,
                  fontSize: 12,
                  fontWeight: FontWeight.bold),
              overflow: TextOverflow.ellipsis),
        ],
      ),
    );
  }

  // ════════════════════════════════════════════════════════════════════════
  // LOADING
  // ════════════════════════════════════════════════════════════════════════
  Widget _buildLoadingOverlay() {
    return Container(
      color: Colors.white,
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: _primary.withOpacity(0.1),
                shape: BoxShape.circle,
              ),
              child: const CircularProgressIndicator(
                  color: _primary, strokeWidth: 3),
            ),
            const SizedBox(height: 20),
            const Text(
              'Locating bus...',
              style: TextStyle(
                  color: Colors.black54,
                  fontSize: 16,
                  fontWeight: FontWeight.w500),
            ),
            const SizedBox(height: 6),
            Text(
              'Connecting to driver GPS',
              style: TextStyle(color: Colors.grey.shade400, fontSize: 13),
            ),
          ],
        ),
      ),
    );
  }

  // ════════════════════════════════════════════════════════════════════════
  // ERROR STATE
  // ════════════════════════════════════════════════════════════════════════
  Widget _buildErrorState() {
    return Container(
      color: Colors.white.withOpacity(0.92),
      child: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                padding: const EdgeInsets.all(24),
                decoration: BoxDecoration(
                  color: Colors.orange.shade50,
                  shape: BoxShape.circle,
                ),
                child: Icon(Icons.location_off_rounded,
                    size: 56, color: Colors.orange.shade400),
              ),
              const SizedBox(height: 20),
              const Text(
                'Location Unavailable',
                style: TextStyle(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                    color: Colors.black87),
              ),
              const SizedBox(height: 8),
              Text(
                widget.student.assignedDriverId == null
                    ? 'No driver has been assigned to ${widget.student.fullName} yet.'
                    : 'The driver\'s location could not be retrieved. Please try again.',
                textAlign: TextAlign.center,
                style:
                    TextStyle(color: Colors.grey.shade600, fontSize: 14),
              ),
              const SizedBox(height: 28),
              ElevatedButton.icon(
                onPressed: _fetchLocation,
                icon: const Icon(Icons.refresh, color: Colors.white),
                label: const Text('Retry',
                    style: TextStyle(
                        color: Colors.white, fontWeight: FontWeight.bold)),
                style: ElevatedButton.styleFrom(
                  backgroundColor: _primary,
                  padding: const EdgeInsets.symmetric(
                      horizontal: 28, vertical: 14),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(14),
                  ),
                  elevation: 0,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
