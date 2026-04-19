import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../services/driver_service.dart';
import '../services/student_service.dart';
import 'edit_driver_profile_screen.dart';

class DriverProfileScreen extends StatefulWidget {
  const DriverProfileScreen({super.key});

  @override
  State<DriverProfileScreen> createState() => _DriverProfileScreenState();
}

class _DriverProfileScreenState extends State<DriverProfileScreen> {
  final DriverService driverService = DriverService();

  bool isLoading = true;
  String? driverId;
  DriverProfile? profile;
  List<Student> students = [];

  @override
  void initState() {
    super.initState();
    loadProfileData();
  }

  Future<void> loadProfileData() async {
    setState(() => isLoading = true);

    SharedPreferences prefs = await SharedPreferences.getInstance();
    driverId = prefs.getString('uid');

    if (driverId != null) {
      profile = await driverService.getDriverProfile(driverId!);
      students = await driverService.getAssignedStudents(driverId!);
    }

    setState(() => isLoading = false);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF4F7FD),
      appBar: AppBar(
        title: const Text(
          'My Profile',
          style: TextStyle(
            color: Color(0xFF0F172A),
            fontWeight: FontWeight.bold,
          ),
        ),
        backgroundColor: Colors.transparent,
        elevation: 0,
        iconTheme: const IconThemeData(color: Color(0xFF0F172A)),
        centerTitle: true,
        actions: [
          if (profile != null)
            Padding(
              padding: const EdgeInsets.only(right: 8.0),
              child: TextButton.icon(
                onPressed: () async {
                  bool? updated = await Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (_) => EditDriverProfileScreen(
                        profile: profile!,
                        driverId: driverId!,
                      ),
                    ),
                  );
                  if (updated == true) loadProfileData();
                },
                icon: const Icon(
                  Icons.edit_rounded,
                  color: Color(0xFF14B8A6),
                  size: 20,
                ),
                label: const Text(
                  'Edit',
                  style: TextStyle(
                    color: Color(0xFF14B8A6),
                    fontWeight: FontWeight.bold,
                    fontSize: 16,
                  ),
                ),
              ),
            ),
        ],
      ),
      body: isLoading
          ? const Center(
              child: CircularProgressIndicator(color: Color(0xFF14B8A6)),
            )
          : profile == null
          ? const Center(child: Text("Could not load profile data."))
          : RefreshIndicator(
              onRefresh: loadProfileData,
              color: const Color(0xFF14B8A6),
              child: ListView(
                physics: const AlwaysScrollableScrollPhysics(),
                padding: const EdgeInsets.only(bottom: 40),
                children: [
                  // Profile Header Section (Vibrant)
                  Container(
                    margin: const EdgeInsets.symmetric(
                      horizontal: 20,
                      vertical: 10,
                    ),
                    padding: const EdgeInsets.all(24),
                    decoration: BoxDecoration(
                      gradient: const LinearGradient(
                        colors: [Color(0xFF14B8A6), Color(0xFF0284C7)],
                      ),
                      borderRadius: BorderRadius.circular(24),
                      boxShadow: [
                        BoxShadow(
                          color: const Color(0xFF14B8A6).withOpacity(0.3),
                          blurRadius: 15,
                          offset: const Offset(0, 8),
                        ),
                      ],
                    ),
                    child: Column(
                      children: [
                        Container(
                          padding: const EdgeInsets.all(4),
                          decoration: const BoxDecoration(
                            color: Colors.white,
                            shape: BoxShape.circle,
                          ),
                          child: CircleAvatar(
                            radius: 46,
                            backgroundColor: const Color(0xFFF0FDF4),
                            backgroundImage:
                                (profile?.profileImageBase64 != null &&
                                    profile!.profileImageBase64!.isNotEmpty)
                                ? MemoryImage(
                                        base64Decode(
                                          profile!.profileImageBase64!,
                                        ),
                                      )
                                      as ImageProvider
                                : null,
                            child:
                                (profile?.profileImageBase64 == null ||
                                    profile!.profileImageBase64!.isEmpty)
                                ? const Icon(
                                    Icons.directions_bus_filled_rounded,
                                    size: 46,
                                    color: Color(0xFF14B8A6),
                                  )
                                : null,
                          ),
                        ),
                        const SizedBox(height: 16),
                        Text(
                          profile?.fullName ?? 'No Name Provided',
                          style: const TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                            color: Colors.white,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 12,
                            vertical: 6,
                          ),
                          decoration: BoxDecoration(
                            color: Colors.white.withOpacity(0.2),
                            borderRadius: BorderRadius.circular(20),
                          ),
                          child: Text(
                            'Vehicle: ${profile?.vehicleNumber ?? "Not Set"}',
                            style: const TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),

                  Padding(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 24.0,
                      vertical: 20,
                    ),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Contact Details Section
                        const Text(
                          'Contact Information',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                            color: Color(0xFF0F172A),
                          ),
                        ),
                        const SizedBox(height: 16),
                        Container(
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(16),
                            boxShadow: [
                              BoxShadow(
                                color: Colors.blueGrey.withOpacity(0.06),
                                blurRadius: 10,
                                offset: const Offset(0, 4),
                              ),
                            ],
                            border: Border.all(color: Colors.grey.shade100),
                          ),
                          child: Column(
                            children: [
                              _buildInfoRow(
                                Icons.phone_rounded,
                                'Phone Number',
                                profile?.phoneNumber ?? 'Not Set',
                              ),
                              const Divider(
                                height: 1,
                                indent: 60,
                                endIndent: 20,
                                color: Color(0xFFF1F5F9),
                              ),
                              _buildInfoRow(
                                Icons.badge_rounded,
                                'License Number',
                                profile?.licenseNumber ?? 'Not Set',
                              ),
                            ],
                          ),
                        ),

                        const SizedBox(height: 32),

                        // Registered Students Section
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text(
                              'Assigned Students',
                              style: TextStyle(
                                fontSize: 18,
                                fontWeight: FontWeight.bold,
                                color: Color(0xFF0F172A),
                              ),
                            ),
                            Container(
                              padding: const EdgeInsets.symmetric(
                                horizontal: 12,
                                vertical: 4,
                              ),
                              decoration: BoxDecoration(
                                color: const Color(0xFFF0FDF4),
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: Text(
                                '${students.length}',
                                style: const TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: Color(0xFF14B8A6),
                                  fontSize: 16,
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 16),

                        if (students.isEmpty)
                          const Center(
                            child: Padding(
                              padding: EdgeInsets.all(20.0),
                              child: Text(
                                "No students currently assigned to your route.",
                                style: TextStyle(color: Color(0xFF64748B)),
                              ),
                            ),
                          )
                        else
                          ...students.map(
                            (student) => Container(
                              margin: const EdgeInsets.only(bottom: 12),
                              decoration: BoxDecoration(
                                color: Colors.white,
                                borderRadius: BorderRadius.circular(16),
                                boxShadow: [
                                  BoxShadow(
                                    color: Colors.blueGrey.withOpacity(0.06),
                                    blurRadius: 10,
                                    offset: const Offset(0, 4),
                                  ),
                                ],
                                border: Border.all(color: Colors.grey.shade100),
                              ),
                              child: ListTile(
                                contentPadding: const EdgeInsets.all(12),
                                leading: Container(
                                  padding: const EdgeInsets.all(10),
                                  decoration: BoxDecoration(
                                    color: const Color(0xFFF0FDF4),
                                    borderRadius: BorderRadius.circular(12),
                                  ),
                                  child: const Icon(
                                    Icons.school_rounded,
                                    color: Color(0xFF14B8A6),
                                    size: 24,
                                  ),
                                ),
                                title: Text(
                                  student.fullName,
                                  style: const TextStyle(
                                    fontWeight: FontWeight.bold,
                                    fontSize: 16,
                                    color: Color(0xFF0F172A),
                                  ),
                                ),
                                subtitle: Text(
                                  '${student.schoolName} • Status: ${student.status.replaceAll('_', ' ')}',
                                  style: const TextStyle(
                                    color: Color(0xFF64748B),
                                  ),
                                ),
                              ),
                            ),
                          ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
    );
  }

  Widget _buildInfoRow(IconData icon, String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: const Color(0xFFF1F5F9),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(icon, color: const Color(0xFF64748B), size: 20),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: const TextStyle(
                    fontSize: 13,
                    color: Color(0xFF94A3B8),
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  value,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                    color: Color(0xFF1E293B),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
