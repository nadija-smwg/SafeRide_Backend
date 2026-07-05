import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'add_student_to_driver_screen.dart';
import 'route_list_screen.dart';
import 'driver_profile_screen.dart';
import '../services/driver_service.dart';

class DriverHomeScreen extends StatefulWidget {
  const DriverHomeScreen({super.key});

  @override
  State<DriverHomeScreen> createState() => _DriverHomeScreenState();
}

class _DriverHomeScreenState extends State<DriverHomeScreen> with SingleTickerProviderStateMixin {
  late AnimationController _animController;
  late Animation<double> _fadeAnim;

  final DriverService _driverService = DriverService();
  String? _driverId;

  // Session mode: "NONE", "MORNING", or "AFTERNOON"
  String _activeSessionMode = "NONE";
  bool _isLoadingSession = true;
  bool _isTogglingSession = false;

  @override
  void initState() {
    super.initState();
    _animController = AnimationController(vsync: this, duration: const Duration(milliseconds: 800));
    _fadeAnim = CurvedAnimation(parent: _animController, curve: Curves.easeOutCubic);
    _animController.forward();
    _loadSessionMode();
  }

  @override
  void dispose() {
    _animController.dispose();
    super.dispose();
  }

  Future<void> _loadSessionMode() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    _driverId = prefs.getString('uid');
    if (_driverId != null) {
      String? mode = await _driverService.getSessionMode(_driverId!);
      if (mounted) {
        setState(() {
          _activeSessionMode = mode ?? "NONE";
          _isLoadingSession = false;
        });
      }
    } else {
      if (mounted) setState(() => _isLoadingSession = false);
    }
  }

  Future<void> _toggleSession(String mode) async {
    if (_isTogglingSession || _driverId == null) return;
    setState(() => _isTogglingSession = true);

    // If the same mode is already active, turn it off
    String newMode = (_activeSessionMode == mode) ? "NONE" : mode;

    bool success = await _driverService.setSessionMode(_driverId!, newMode);
    if (mounted) {
      if (success) {
        setState(() {
          _activeSessionMode = newMode;
          _isTogglingSession = false;
        });
      } else {
        setState(() => _isTogglingSession = false);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text("Failed to update session mode."),
            backgroundColor: Colors.redAccent,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    bool isMorningActive = _activeSessionMode == "MORNING";
    bool isAfternoonActive = _activeSessionMode == "AFTERNOON";
    bool noSessionActive = _activeSessionMode == "NONE";

    return PopScope(
      canPop: false,
      onPopInvoked: (didPop) {
        if (didPop) return;
        SystemNavigator.pop();
      },
      child: Scaffold(
        backgroundColor: const Color(0xFFF4F7FD),
        body: CustomScrollView(
          physics: const BouncingScrollPhysics(),
          slivers: [
            SliverAppBar(
              automaticallyImplyLeading: false,
              expandedHeight: 380.0,
              floating: false,
              pinned: true,
              elevation: 4,
              shadowColor: Colors.black45,
              backgroundColor: const Color(0xFF14B8A6), 
              title: const Text(
                "Driver Dashboard",
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.w800, 
                  fontSize: 24, 
                  letterSpacing: 1.2,
                  fontStyle: FontStyle.italic,
                  shadows: [Shadow(color: Colors.black87, blurRadius: 4)]
                ),
              ),
              actions: [
                _buildProfileMenu(context),
              ],
              flexibleSpace: FlexibleSpaceBar(
                collapseMode: CollapseMode.parallax,
                background: Stack(
                  fit: StackFit.expand,
                  children: [
                    Image.asset(
                      'assets/images/bus.png',
                      fit: BoxFit.cover,
                    ),
                    Positioned(
                      top: 0,
                      left: 0,
                      right: 0,
                      height: 100,
                      child: Container(
                        decoration: BoxDecoration(
                          gradient: LinearGradient(
                            begin: Alignment.topCenter,
                            end: Alignment.bottomCenter,
                            colors: [Colors.black.withOpacity(0.5), Colors.transparent],
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            SliverToBoxAdapter(
              child: FadeTransition(
                opacity: _fadeAnim,
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 24.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        crossAxisAlignment: CrossAxisAlignment.center,
                        children: [
                          const Icon(Icons.directions_bus_filled_rounded, color: Color(0xFF14B8A6), size: 28),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                const Text(
                                  "Safe travels!",
                                  style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Color(0xFF0F172A)),
                                ),
                                const SizedBox(height: 2),
                                Text(
                                  "Let's hit the road today.",
                                  style: TextStyle(fontSize: 15, color: Colors.blueGrey.shade600),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 24),

                      // ── Session Mode Status Banner ──
                      _buildSessionStatusBanner(isMorningActive, isAfternoonActive),

                      const SizedBox(height: 28),
                      const Text(
                        "Route Sessions",
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFF64748B), letterSpacing: 0.5),
                      ),
                      const SizedBox(height: 6),
                      Text(
                        noSessionActive
                            ? "Enable a session to start pickup or dropoff"
                            : isMorningActive
                                ? "Morning session is active"
                                : "Afternoon session is active",
                        style: TextStyle(fontSize: 13, color: Colors.blueGrey.shade400),
                      ),
                      const SizedBox(height: 16),

                      // ── Morning Session Card with Toggle ──
                      _buildSessionCard(
                        context,
                        title: "Morning Pickup",
                        description: "Standard morning route",
                        icon: Icons.wb_sunny_rounded,
                        accentColor: const Color(0xFFF59E0B),
                        isActive: isMorningActive,
                        isDisabled: isAfternoonActive,
                        isPickup: true,
                        onToggle: () => _toggleSession("MORNING"),
                      ),
                      const SizedBox(height: 16),

                      // ── Afternoon Session Card with Toggle ──
                      _buildSessionCard(
                        context,
                        title: "Afternoon Dropoff",
                        description: "Standard dropoff route",
                        icon: Icons.nights_stay_rounded,
                        accentColor: const Color(0xFF6366F1),
                        isActive: isAfternoonActive,
                        isDisabled: isMorningActive,
                        isPickup: false,
                        onToggle: () => _toggleSession("AFTERNOON"),
                      ),
                      const SizedBox(height: 100),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
        floatingActionButton: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            FloatingActionButton.extended(
              heroTag: "broadcastFAB",
              backgroundColor: const Color(0xFF14B8A6),
              icon: const Icon(Icons.campaign_rounded, color: Colors.white),
              label: const Text("Broadcast", style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
              onPressed: () => _showBroadcastDialog(context),
            ),
            const SizedBox(height: 12),
            FloatingActionButton.extended(
              heroTag: "linkStudentFAB",
              onPressed: () {
                Navigator.push(context, MaterialPageRoute(builder: (_) => const AddStudentToDriverScreen()));
              },
              backgroundColor: const Color(0xFF14B8A6),
              elevation: 6,
              icon: const Icon(Icons.person_add_alt_1_rounded, color: Colors.white),
              label: const Text('Link Student', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16)),
            ),
          ],
        ),
      ),
    );
  }

  // ── Session Status Banner ──
  Widget _buildSessionStatusBanner(bool isMorningActive, bool isAfternoonActive) {
    if (_isLoadingSession) {
      return Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.grey.shade100,
          borderRadius: BorderRadius.circular(14),
        ),
        child: const Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2)),
            SizedBox(width: 12),
            Text("Loading session state...", style: TextStyle(color: Colors.grey)),
          ],
        ),
      );
    }

    Color bannerColor;
    IconData bannerIcon;
    String bannerText;

    if (isMorningActive) {
      bannerColor = const Color(0xFFF59E0B);
      bannerIcon = Icons.wb_sunny_rounded;
      bannerText = "Morning Session Active";
    } else if (isAfternoonActive) {
      bannerColor = const Color(0xFF6366F1);
      bannerIcon = Icons.nights_stay_rounded;
      bannerText = "Afternoon Session Active";
    } else {
      bannerColor = const Color(0xFF94A3B8);
      bannerIcon = Icons.power_settings_new_rounded;
      bannerText = "No Session Active";
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [bannerColor.withOpacity(0.15), bannerColor.withOpacity(0.05)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: bannerColor.withOpacity(0.3)),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: bannerColor.withOpacity(0.15),
              shape: BoxShape.circle,
            ),
            child: Icon(bannerIcon, color: bannerColor, size: 22),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  bannerText,
                  style: TextStyle(
                    fontSize: 15,
                    fontWeight: FontWeight.bold,
                    color: bannerColor,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  isMorningActive || isAfternoonActive
                      ? "Student statuses can be changed"
                      : "Enable a session to change statuses",
                  style: TextStyle(fontSize: 12, color: bannerColor.withOpacity(0.7)),
                ),
              ],
            ),
          ),
          if (isMorningActive || isAfternoonActive)
            Container(
              width: 10,
              height: 10,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: Colors.green,
                boxShadow: [
                  BoxShadow(color: Colors.green.withOpacity(0.4), blurRadius: 6),
                ],
              ),
            ),
        ],
      ),
    );
  }

  // ── Session Card with Toggle ──
  Widget _buildSessionCard(BuildContext context, {
    required String title,
    required String description,
    required IconData icon,
    required Color accentColor,
    required bool isActive,
    required bool isDisabled,
    required bool isPickup,
    required VoidCallback onToggle,
  }) {
    // Card is only tappable when this session is active
    bool canNavigate = isActive;
    double opacity = isDisabled ? 0.45 : 1.0;

    return AnimatedOpacity(
      opacity: opacity,
      duration: const Duration(milliseconds: 300),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: isActive
                  ? accentColor.withOpacity(0.15)
                  : Colors.blueGrey.withOpacity(0.08),
              blurRadius: isActive ? 20 : 15,
              offset: const Offset(0, 8),
            )
          ],
          border: Border.all(
            color: isActive ? accentColor.withOpacity(0.5) : Colors.grey.shade200,
            width: isActive ? 2 : 1,
          ),
        ),
        child: Column(
          children: [
            // Top row: icon + info + toggle
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: accentColor.withOpacity(isDisabled ? 0.05 : 0.1),
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: Icon(icon, color: isDisabled ? Colors.grey : accentColor, size: 28),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        title,
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                          color: isDisabled ? Colors.grey : const Color(0xFF1E293B),
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        isActive
                            ? "Session active — tap to open"
                            : isDisabled
                                ? "Disabled while other session is active"
                                : description,
                        style: TextStyle(
                          fontSize: 13,
                          color: isActive
                              ? accentColor
                              : isDisabled
                                  ? Colors.grey.shade400
                                  : const Color(0xFF64748B),
                        ),
                      ),
                    ],
                  ),
                ),
                // Toggle switch
                _isTogglingSession
                    ? const SizedBox(
                        width: 24,
                        height: 24,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : Switch(
                        value: isActive,
                        onChanged: isDisabled ? null : (_) => onToggle(),
                        activeColor: accentColor,
                        activeTrackColor: accentColor.withOpacity(0.3),
                      ),
              ],
            ),

            // Navigate button when active
            if (canNavigate) ...[
              const SizedBox(height: 12),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton.icon(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(builder: (_) => RouteListScreen(isPickup: isPickup)),
                    );
                  },
                  icon: const Icon(Icons.arrow_forward_rounded, size: 18),
                  label: Text(isPickup ? "Open Pickup Route" : "Open Dropoff Route"),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: accentColor,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    elevation: 0,
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildProfileMenu(BuildContext context) {
    return PopupMenuButton<String>(
      icon: const Icon(Icons.account_circle_rounded, color: Colors.white, size: 36, shadows: [Shadow(color: Colors.black45, blurRadius: 4)]),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      offset: const Offset(0, 50),
      onSelected: (value) async {
        if (value == 'profile') {
          Navigator.push(context, MaterialPageRoute(builder: (_) => const DriverProfileScreen()));
        } else if (value == 'logout') {
          // Clear session mode on logout
          if (_driverId != null) {
            await _driverService.setSessionMode(_driverId!, "NONE");
          }
          SharedPreferences prefs = await SharedPreferences.getInstance();
          await prefs.remove('uid');
          await prefs.remove('role');
          if (!context.mounted) return;
          Navigator.pushNamedAndRemoveUntil(context, '/Driverlogin', (route) => false);
        }
      },
      itemBuilder: (BuildContext context) => <PopupMenuEntry<String>>[
        const PopupMenuItem<String>(
          value: 'profile',
          child: Row(
            children: [
              Icon(Icons.person_outline, color: Colors.black87),
              SizedBox(width: 12),
              Text('My Profile', style: TextStyle(fontWeight: FontWeight.w600)),
            ],
          ),
        ),
        const PopupMenuDivider(),
        const PopupMenuItem<String>(
          value: 'logout',
          child: Row(
            children: [
              Icon(Icons.logout, color: Colors.redAccent),
              SizedBox(width: 12),
              Text('Logout', style: TextStyle(color: Colors.redAccent, fontWeight: FontWeight.w600)),
            ],
          ),
        ),
      ],
    );
  }

  void _showBroadcastDialog(BuildContext context) {
    final titleController = TextEditingController();
    final messageController = TextEditingController();
    bool isSending = false;

    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) => StatefulBuilder(
        builder: (context, setState) {
          return AlertDialog(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
            title: const Row(
              children: [
                Icon(Icons.campaign_rounded, color: Color(0xFF14B8A6)),
                SizedBox(width: 10),
                Text("Broadcast Message", style: TextStyle(fontWeight: FontWeight.bold, fontSize: 20)),
              ],
            ),
            content: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextField(
                  controller: titleController,
                  decoration: InputDecoration(
                    labelText: "Subject (e.g. Bus Delayed)",
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                  ),
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: messageController,
                  maxLines: 3,
                  decoration: InputDecoration(
                    labelText: "Message",
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                  ),
                ),
              ],
            ),
            actions: [
              TextButton(
                onPressed: isSending ? null : () => Navigator.pop(context),
                child: const Text("Cancel", style: TextStyle(color: Colors.grey)),
              ),
              ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF14B8A6),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
                onPressed: isSending
                    ? null
                    : () async {
                        if (titleController.text.trim().isEmpty || messageController.text.trim().isEmpty) {
                          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Please fill all fields")));
                          return;
                        }
                        setState(() => isSending = true);
                        SharedPreferences prefs = await SharedPreferences.getInstance();
                        String? driverId = prefs.getString('userId');
                        if (driverId != null) {
                          bool success = await DriverService().sendBroadcastMessage(
                              driverId, titleController.text.trim(), messageController.text.trim());
                          if (success) {
                            Navigator.pop(context);
                            ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Broadcast Sent!", style: TextStyle(color: Colors.white)), backgroundColor: Colors.green));
                          } else {
                            setState(() => isSending = false);
                            ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Failed to send broadcast", style: TextStyle(color: Colors.white)), backgroundColor: Colors.red));
                          }
                        }
                      },
                child: isSending
                    ? const SizedBox(width: 20, height: 20, child: CircularProgressIndicator(color: Colors.white, strokeWidth: 2))
                    : const Text("Send", style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
              ),
            ],
          );
        }
      ),
    );
  }
}