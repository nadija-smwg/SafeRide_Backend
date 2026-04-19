import 'dart:async';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:geolocator/geolocator.dart';
import '../services/driver_service.dart';
import '../services/student_service.dart';
import '../services/location_service.dart';
import 'qr_scanner_screen.dart';

class RouteListScreen extends StatefulWidget {
  final bool isPickup;
  const RouteListScreen({super.key, required this.isPickup});

  @override
  State<RouteListScreen> createState() => _RouteListScreenState();
}

class _RouteListScreenState extends State<RouteListScreen>
    with TickerProviderStateMixin {
  final DriverService driverService = DriverService();
  List<Student> students = [];
  bool isLoading = true;
  String? driverId;

  // GPS broadcasting
  StreamSubscription<Position>? _positionSub;
  bool isBroadcasting = false;
  double? currentLat;
  double? currentLng;
  Timer? _broadcastTimer;

  // Animation for the broadcasting pulse
  late AnimationController _pulseController;
  late Animation<double> _pulseAnimation;

  // Theme colors
  late Color _accentColor;
  late Color _accentLight;
  late String _modeLabel;
  late IconData _modeIcon;

  @override
  void initState() {
    super.initState();
    _accentColor =
        widget.isPickup ? const Color(0xFFFF8C42) : const Color(0xFF5C6BC0);
    _accentLight =
        widget.isPickup ? const Color(0xFFFFF3E0) : const Color(0xFFE8EAF6);
    _modeLabel = widget.isPickup ? 'Morning Pickup' : 'Afternoon Dropoff';
    _modeIcon = widget.isPickup ? Icons.wb_sunny_rounded : Icons.nights_stay_rounded;

    _pulseController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1200),
    )..repeat(reverse: true);
    _pulseAnimation =
        Tween<double>(begin: 0.85, end: 1.0).animate(_pulseController);

    loadRoute();
    _startLocationBroadcast();
  }

  @override
  void dispose() {
    _positionSub?.cancel();
    _broadcastTimer?.cancel();
    _pulseController.dispose();
    super.dispose();
  }

  Future<void> _startLocationBroadcast() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    driverId = prefs.getString('uid');
    if (driverId == null) return;

    // Get initial position
    final pos = await LocationService.getCurrentPosition();
    if (pos != null && mounted) {
      setState(() {
        currentLat = pos.latitude;
        currentLng = pos.longitude;
        isBroadcasting = true;
      });
      await driverService.updateDriverLocation(
          driverId!, pos.latitude, pos.longitude);
    }

    // Stream real-time position updates
    _positionSub = LocationService.positionStream().listen((pos) async {
      if (!mounted) return;
      setState(() {
        currentLat = pos.latitude;
        currentLng = pos.longitude;
        isBroadcasting = true;
      });
      if (driverId != null) {
        await driverService.updateDriverLocation(
            driverId!, pos.latitude, pos.longitude);
      }
    });
  }

  Future<void> loadRoute() async {
    setState(() => isLoading = true);
    SharedPreferences prefs = await SharedPreferences.getInstance();
    driverId = prefs.getString('uid');
    if (driverId != null) {
      students = await driverService.getAssignedStudents(driverId!);
    }
    setState(() => isLoading = false);
  }

  Future<void> scanQR() async {
    final String? scannedStudentId = await Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const QRScannerScreen()),
    );

    if (scannedStudentId != null && scannedStudentId.isNotEmpty) {
      Student? targetStudent;
      try {
        targetStudent = students.firstWhere((s) => s.id == scannedStudentId);
      } catch (e) {}

      if (targetStudent != null) {
        bool isCompleted = widget.isPickup
            ? targetStudent.status == 'IN_SCHOOL'
            : targetStudent.status == 'AT_HOME';
        if (isCompleted) {
          if (!mounted) return;
          _showAlreadyDroppedDialog();
          return;
        }
      }

      setState(() => isLoading = true);
      bool success =
          await driverService.scanStudent(scannedStudentId, widget.isPickup);
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Row(
            children: [
              Icon(
                success ? Icons.check_circle : Icons.error,
                color: Colors.white,
              ),
              const SizedBox(width: 8),
              Text(success
                  ? 'Student scanned successfully!'
                  : 'Failed to update status. Check ID.'),
            ],
          ),
          backgroundColor: success ? Colors.green : Colors.redAccent,
          behavior: SnackBarBehavior.floating,
          shape:
              RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
      );
      loadRoute();
    }
  }

  void _showAlreadyDroppedDialog() {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: Row(
          children: const [
            Icon(Icons.info_outline, color: Colors.orange),
            SizedBox(width: 8),
            Text('Already Done', style: TextStyle(fontWeight: FontWeight.bold)),
          ],
        ),
        content:
            const Text('This student has already been dropped off today.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(),
            child: Text('OK',
                style: TextStyle(color: _accentColor, fontWeight: FontWeight.bold)),
          )
        ],
      ),
    );
  }

  String _getTodayName() {
    const days = [
      'Monday', 'Tuesday', 'Wednesday', 'Thursday',
      'Friday', 'Saturday', 'Sunday'
    ];
    return days[DateTime.now().weekday - 1];
  }

  bool _isStudentScheduledForToday(Student s) {
    String today = _getTodayName();
    ScheduleItem? todaySchedule;
    try {
      todaySchedule = s.weeklySchedule.firstWhere((item) => item.day == today);
    } catch (e) {}
    if (todaySchedule == null) return false;
    return widget.isPickup
        ? todaySchedule.needMorningPickup
        : todaySchedule.needEveningPickup;
  }

  @override
  Widget build(BuildContext context) {
    final scheduledStudents =
        students.where((s) => _isStudentScheduledForToday(s)).toList();
    final pending = scheduledStudents
        .where((s) =>
            widget.isPickup ? s.status == 'AT_HOME' : s.status == 'IN_SCHOOL')
        .toList();
    final inTransit =
        scheduledStudents.where((s) => s.status == 'IN_TRANSIT').toList();
    final completed = scheduledStudents
        .where((s) =>
            widget.isPickup ? s.status == 'IN_SCHOOL' : s.status == 'AT_HOME')
        .toList();

    return Scaffold(
      backgroundColor: const Color(0xFFF7F9FC),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        iconTheme: IconThemeData(color: _accentColor),
        title: Row(
          children: [
            Icon(_modeIcon, color: _accentColor, size: 22),
            const SizedBox(width: 8),
            Text(
              _modeLabel,
              style: TextStyle(
                color: _accentColor,
                fontWeight: FontWeight.bold,
                fontSize: 18,
              ),
            ),
          ],
        ),
        actions: [
          if (isBroadcasting)
            Padding(
              padding: const EdgeInsets.only(right: 12),
              child: ScaleTransition(
                scale: _pulseAnimation,
                child: Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                  decoration: BoxDecoration(
                    color: Colors.green.shade50,
                    borderRadius: BorderRadius.circular(20),
                    border: Border.all(color: Colors.green.shade300),
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
                      const SizedBox(width: 5),
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
        ],
      ),
      body: isLoading
          ? Center(
              child: CircularProgressIndicator(color: _accentColor),
            )
          : RefreshIndicator(
              color: _accentColor,
              onRefresh: loadRoute,
              child: CustomScrollView(
                slivers: [
                  // GPS status card
                  SliverToBoxAdapter(
                    child: _buildGpsCard(),
                  ),

                  // Summary chips
                  SliverToBoxAdapter(
                    child: _buildSummaryChips(pending, inTransit, completed),
                  ),

                  // Student sections
                  if (scheduledStudents.isEmpty)
                    SliverFillRemaining(child: _buildEmptyState())
                  else ...[
                    if (pending.isNotEmpty) ...[
                      _buildSectionHeader(
                          'Pending', pending.length, Colors.orange),
                      _buildStudentSliver(pending, Colors.orange),
                    ],
                    if (inTransit.isNotEmpty) ...[
                      _buildSectionHeader(
                          'In Transit', inTransit.length, Colors.blue),
                      _buildStudentSliver(inTransit, Colors.blue),
                    ],
                    if (completed.isNotEmpty) ...[
                      _buildSectionHeader(
                          'Completed', completed.length, Colors.green),
                      _buildStudentSliver(completed, Colors.green),
                    ],
                    const SliverToBoxAdapter(child: SizedBox(height: 100)),
                  ],
                ],
              ),
            ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: scanQR,
        backgroundColor: _accentColor,
        icon: const Icon(Icons.qr_code_scanner, color: Colors.white),
        label: const Text('Scan QR',
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
        elevation: 4,
      ),
    );
  }

  Widget _buildGpsCard() {
    return Container(
      margin: const EdgeInsets.all(16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [_accentColor, _accentColor.withOpacity(0.7)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: _accentColor.withOpacity(0.3),
            blurRadius: 12,
            offset: const Offset(0, 4),
          )
        ],
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.2),
              shape: BoxShape.circle,
            ),
            child: const Icon(Icons.my_location, color: Colors.white, size: 24),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  isBroadcasting
                      ? 'Broadcasting Location'
                      : 'Acquiring GPS...',
                  style: const TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.bold,
                      fontSize: 15),
                ),
                const SizedBox(height: 2),
                Text(
                  currentLat != null
                      ? '${currentLat!.toStringAsFixed(5)}, ${currentLng!.toStringAsFixed(5)}'
                      : 'Waiting for GPS signal...',
                  style: TextStyle(
                    color: Colors.white.withOpacity(0.85),
                    fontSize: 12,
                  ),
                ),
              ],
            ),
          ),
          if (isBroadcasting)
            ScaleTransition(
              scale: _pulseAnimation,
              child: const Icon(Icons.wifi_tethering,
                  color: Colors.white, size: 28),
            )
          else
            const SizedBox(
              width: 20,
              height: 20,
              child: CircularProgressIndicator(
                  color: Colors.white, strokeWidth: 2),
            ),
        ],
      ),
    );
  }

  Widget _buildSummaryChips(
      List<Student> pending, List<Student> inTransit, List<Student> completed) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: Row(
        children: [
          _chip('${pending.length} Pending', Colors.orange),
          const SizedBox(width: 8),
          _chip('${inTransit.length} In Transit', Colors.blue),
          const SizedBox(width: 8),
          _chip('${completed.length} Done', Colors.green),
        ],
      ),
    );
  }

  Widget _chip(String label, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withOpacity(0.4)),
      ),
      child: Text(label,
          style: TextStyle(
              color: color, fontWeight: FontWeight.w600, fontSize: 12)),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.event_available, size: 70, color: Colors.grey.shade300),
          const SizedBox(height: 16),
          Text(
            'No students scheduled today',
            style:
                TextStyle(color: Colors.grey.shade500, fontSize: 16),
          ),
        ],
      ),
    );
  }

  Widget _buildSectionHeader(String title, int count, Color color) {
    return SliverToBoxAdapter(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
        child: Row(
          children: [
            Container(
              width: 4,
              height: 18,
              decoration: BoxDecoration(
                  color: color, borderRadius: BorderRadius.circular(2)),
            ),
            const SizedBox(width: 8),
            Text(
              title,
              style: TextStyle(
                  fontSize: 16, fontWeight: FontWeight.bold, color: color),
            ),
            const SizedBox(width: 8),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
              decoration: BoxDecoration(
                color: color.withOpacity(0.15),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Text('$count',
                  style: TextStyle(
                      color: color,
                      fontWeight: FontWeight.bold,
                      fontSize: 12)),
            ),
          ],
        ),
      ),
    );
  }

  SliverList _buildStudentSliver(List<Student> list, Color color) {
    return SliverList(
      delegate: SliverChildBuilderDelegate(
        (ctx, i) => _buildStudentCard(list[i], color),
        childCount: list.length,
      ),
    );
  }

  Widget _buildStudentCard(Student s, Color statusColor) {
    IconData statusIcon;
    String statusText;
    switch (s.status) {
      case 'IN_TRANSIT':
        statusIcon = Icons.directions_bus;
        statusText = 'In Transit';
        break;
      case 'IN_SCHOOL':
        statusIcon = Icons.school;
        statusText = 'At School';
        break;
      case 'AT_HOME':
        statusIcon = Icons.home;
        statusText = 'At Home';
        break;
      default:
        statusIcon = Icons.home_outlined;
        statusText = s.status.replaceAll('_', ' ');
    }

    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 5),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 8,
            offset: const Offset(0, 2),
          )
        ],
      ),
      child: ListTile(
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        leading: CircleAvatar(
          radius: 24,
          backgroundColor: statusColor.withOpacity(0.15),
          child: Text(
            s.fullName.isNotEmpty ? s.fullName[0].toUpperCase() : '?',
            style: TextStyle(
                color: statusColor,
                fontWeight: FontWeight.bold,
                fontSize: 18),
          ),
        ),
        title: Text(
          s.fullName,
          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
        ),
        subtitle: Row(
          children: [
            Icon(statusIcon, size: 13, color: statusColor),
            const SizedBox(width: 4),
            Text(statusText,
                style: TextStyle(color: statusColor, fontSize: 12)),
          ],
        ),
        trailing: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: statusColor.withOpacity(0.08),
            shape: BoxShape.circle,
          ),
          child: Icon(statusIcon, color: statusColor, size: 20),
        ),
      ),
    );
  }
}
