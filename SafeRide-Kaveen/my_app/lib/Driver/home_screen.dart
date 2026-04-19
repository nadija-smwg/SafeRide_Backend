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

  @override
  void initState() {
    super.initState();
    _animController = AnimationController(vsync: this, duration: const Duration(milliseconds: 800));
    _fadeAnim = CurvedAnimation(parent: _animController, curve: Curves.easeOutCubic);
    _animController.forward();
  }

  @override
  void dispose() {
    _animController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
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
                      const SizedBox(height: 32),
                      const Text(
                        "Route Sessions",
                        style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFF64748B), letterSpacing: 0.5),
                      ),
                      const SizedBox(height: 16),
                      _buildModeCard(
                        context,
                        title: "Morning Pickup",
                        description: "Standard morning route",
                        icon: Icons.wb_sunny_rounded,
                        accentColor: const Color(0xFFF59E0B),
                        isPickup: true,
                      ),
                      const SizedBox(height: 16),
                      _buildModeCard(
                        context,
                        title: "Afternoon Dropoff",
                        description: "Standard dropoff route",
                        icon: Icons.nights_stay_rounded,
                        accentColor: const Color(0xFF6366F1),
                        isPickup: false,
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

  Widget _buildProfileMenu(BuildContext context) {
    return PopupMenuButton<String>(
      icon: const Icon(Icons.account_circle_rounded, color: Colors.white, size: 36, shadows: [Shadow(color: Colors.black45, blurRadius: 4)]),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      offset: const Offset(0, 50),
      onSelected: (value) async {
        if (value == 'profile') {
          Navigator.push(context, MaterialPageRoute(builder: (_) => const DriverProfileScreen()));
        } else if (value == 'logout') {
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

  Widget _buildModeCard(BuildContext context, {
    required String title,
    required String description,
    required IconData icon,
    required Color accentColor,
    required bool isPickup,
  }) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: () {
          Navigator.push(context, MaterialPageRoute(builder: (_) => RouteListScreen(isPickup: isPickup)));
        },
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            boxShadow: [
              BoxShadow(
                color: Colors.blueGrey.withOpacity(0.08),
                blurRadius: 15,
                offset: const Offset(0, 8),
              )
            ],
            border: Border.all(color: Colors.grey.shade200, width: 1),
          ),
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.all(14),
                decoration: BoxDecoration(
                  color: accentColor.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(14),
                ),
                child: Icon(icon, color: accentColor, size: 28),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFF1E293B)),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      description,
                      style: const TextStyle(fontSize: 14, color: Color(0xFF64748B)),
                    ),
                  ],
                ),
              ),
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: const Color(0xFFF8FAFC),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: Colors.grey.shade200),
                ),
                child: const Icon(Icons.arrow_forward_ios_rounded, color: Color(0xFF94A3B8), size: 16),
              )
            ],
          ),
        ),
      ),
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