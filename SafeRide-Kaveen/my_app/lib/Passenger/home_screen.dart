import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../services/student_service.dart';
import 'add_student_screen.dart';
import 'student_details_screen.dart';
import 'live_tracking_screen.dart';
import 'parent_profile_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with SingleTickerProviderStateMixin {
  final StudentService studentService = StudentService();
  List<Student> students = [];
  bool isLoading = true;
  String? parentId;
  late AnimationController _fadeController;
  late Animation<double> _fadeAnimation;

  @override
  void initState() {
    super.initState();
    _fadeController = AnimationController(vsync: this, duration: const Duration(milliseconds: 700));
    _fadeAnimation = CurvedAnimation(parent: _fadeController, curve: Curves.easeOut);
    loadStudents();
  }

  Future<void> loadStudents() async {
    setState(() => isLoading = true);
    SharedPreferences prefs = await SharedPreferences.getInstance();
    parentId = prefs.getString('uid');
    
    if (parentId != null) {
      students = await studentService.getStudentsByParent(parentId!);
    }
    setState(() => isLoading = false);
    _fadeController.forward(from: 0);
  }

  Color _getStatusColor(String status) {
    if (status == 'IN_TRANSIT') return const Color(0xFFFF9800); // Vibrant orange
    if (status == 'IN_SCHOOL') return const Color(0xFF4CAF50); // Fresh green
    return const Color(0xFF2196F3); // Friendly blue
  }

  @override
  void dispose() {
    _fadeController.dispose();
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
        body: RefreshIndicator(
          color: const Color(0xFFFF9800),
          onRefresh: loadStudents,
          child: CustomScrollView(
            slivers: [
              SliverAppBar(
                automaticallyImplyLeading: false,
                expandedHeight: 380.0,
                floating: false,
                pinned: true,
                elevation: 4,
                shadowColor: Colors.black45,
                backgroundColor: const Color(0xFF3B82F6),
                title: const Text(
                  "SafeRide",
                  style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 22, shadows: [Shadow(color: Colors.black54, blurRadius: 2)]),
                ),
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
                actions: [
                  _buildProfileMenu(context),
                ],
              ),
              SliverToBoxAdapter(
                child: isLoading 
                  ? const Padding(
                      padding: EdgeInsets.only(top: 100),
                      child: Center(child: CircularProgressIndicator(color: Color(0xFFFF9800))),
                    )
                  : FadeTransition(
                      opacity: _fadeAnimation,
                      child: Padding(
                        padding: const EdgeInsets.all(20.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              crossAxisAlignment: CrossAxisAlignment.center,
                              children: [
                                const Icon(Icons.family_restroom_rounded, color: Color(0xFF3B82F6), size: 28),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      const Text(
                                        "Track seamlessly!",
                                        style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Color(0xFF0F172A)),
                                      ),
                                      const SizedBox(height: 2),
                                      Text(
                                        "Keep an eye on your children.",
                                        style: TextStyle(fontSize: 15, color: Colors.blueGrey.shade600),
                                      ),
                                    ],
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 32),
                            const Text(
                              'My Students', 
                              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Color(0xFF64748B), letterSpacing: 0.5)
                            ),
                            const SizedBox(height: 16),
                            if (students.isEmpty)
                              _buildEmptyState()
                            else
                              ...students.map((s) => _buildStudentCard(s)),
                            const SizedBox(height: 100), // padding for fab
                          ],
                        ),
                      ),
                    ),
              ),
            ],
          ),
        ),
        floatingActionButton: FloatingActionButton.extended(
          backgroundColor: const Color(0xFFFF9800),
          onPressed: () async {
            bool? added = await Navigator.push(
              context,
              MaterialPageRoute(builder: (context) => const AddStudentScreen()),
            );
            if (added == true) loadStudents(); 
          },
          icon: const Icon(Icons.add, color: Colors.white),
          label: const Text('Add Student', style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
        ),
      ),
    );
  }

  Widget _buildProfileMenu(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(right: 8.0, top: 8.0),
      child: PopupMenuButton<String>(
        icon: const Icon(Icons.account_circle_rounded, color: Colors.white, size: 36, shadows: [Shadow(color: Colors.black45, blurRadius: 4)]),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        offset: const Offset(0, 50),
        onSelected: (value) async {
          if (value == 'profile') {
            Navigator.push(context, MaterialPageRoute(builder: (_) => const ParentProfileScreen()));
          } else if (value == 'logout') {
            SharedPreferences prefs = await SharedPreferences.getInstance();
            await prefs.remove('uid');
            await prefs.remove('role');
            if (!context.mounted) return;
            Navigator.pushNamedAndRemoveUntil(context, '/Passengerlogin', (route) => false);
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
      ),
    );
  }

  Widget _buildStudentCard(Student student) {
    final statusColor = _getStatusColor(student.status);
    
    return Padding(
      padding: const EdgeInsets.only(bottom: 16.0),
      child: Container(
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
        child: Material(
          color: Colors.transparent,
          child: InkWell(
            borderRadius: BorderRadius.circular(16),
            onTap: () async {
              bool? updated = await Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => StudentDetailsScreen(student: student)),
              );
              if (updated == true) loadStudents();
            },
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    children: [
                      Hero(
                        tag: 'student_avatar_${student.id}',
                        child: Container(
                          padding: const EdgeInsets.all(14),
                          decoration: BoxDecoration(
                            color: statusColor.withOpacity(0.1),
                            borderRadius: BorderRadius.circular(14),
                          ),
                          child: Icon(Icons.face_retouching_natural_rounded, color: statusColor, size: 32),
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(student.fullName, style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Color(0xFF0F172A))),
                            const SizedBox(height: 4),
                            Text('${student.schoolName} • Age ${student.age}', style: const TextStyle(color: Color(0xFF64748B), fontSize: 13)),
                            const SizedBox(height: 12),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                              decoration: BoxDecoration(
                                color: statusColor.withOpacity(0.12),
                                borderRadius: BorderRadius.circular(20),
                              ),
                              child: Text(
                                student.status.replaceAll('_', ' '),
                                style: TextStyle(color: statusColor, fontWeight: FontWeight.bold, fontSize: 12),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const Padding(
                    padding: EdgeInsets.symmetric(vertical: 16),
                    child: Divider(height: 1, color: Color(0xFFF1F5F9)),
                  ),
                  // Eye-catching lively button
                  InkWell(
                    onTap: () => Navigator.push(context, MaterialPageRoute(builder: (_) => LiveTrackingScreen(student: student))),
                    borderRadius: BorderRadius.circular(16),
                    child: Container(
                      width: double.infinity,
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      decoration: BoxDecoration(
                        gradient: const LinearGradient(colors: [Color(0xFF3B82F6), Color(0xFF06B6D4)]),
                        borderRadius: BorderRadius.circular(16),
                        boxShadow: [BoxShadow(color: const Color(0xFF3B82F6).withOpacity(0.3), blurRadius: 10, offset: const Offset(0, 4))],
                      ),
                      child: const Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.satellite_alt_rounded, size: 20, color: Colors.white),
                          SizedBox(width: 8),
                          Text(
                            "Watch Live Map",
                            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 15),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          const SizedBox(height: 40),
          Container(
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(color: const Color(0xFFEEF2FF), shape: BoxShape.circle),
            child: const Icon(Icons.family_restroom_rounded, size: 60, color: Color(0xFF6366F1)),
          ),
          const SizedBox(height: 24),
          const Text('No Students Yet', style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Color(0xFF0F172A))),
          const SizedBox(height: 8),
          const Text('Tap the Add Student button below to start tracking your family!', textAlign: TextAlign.center, style: TextStyle(color: Color(0xFF64748B), fontSize: 15)),
        ],
      ),
    );
  }
}