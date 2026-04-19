import 'dart:convert';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import '../services/driver_service.dart';

class EditDriverProfileScreen extends StatefulWidget {
  final DriverProfile profile;
  final String driverId;

  const EditDriverProfileScreen({
    super.key,
    required this.profile,
    required this.driverId,
  });

  @override
  State<EditDriverProfileScreen> createState() => _EditDriverProfileScreenState();
}

class _EditDriverProfileScreenState extends State<EditDriverProfileScreen> {
  late TextEditingController nameController;
  late TextEditingController phoneController;
  late TextEditingController vehicleController;

  bool isSaving = false;
  File? _imageFile;
  String? base64Image;
  final DriverService driverService = DriverService();

  @override
  void initState() {
    super.initState();
    nameController = TextEditingController(text: widget.profile.fullName);
    phoneController = TextEditingController(text: widget.profile.phoneNumber);
    vehicleController = TextEditingController(text: widget.profile.vehicleNumber);
    base64Image = widget.profile.profileImageBase64;
  }

  @override
  void dispose() {
    nameController.dispose();
    phoneController.dispose();
    vehicleController.dispose();
    super.dispose();
  }

  Future<void> _pickImage() async {
    final pickedFile = await ImagePicker().pickImage(source: ImageSource.gallery, imageQuality: 50);
    if (pickedFile != null) {
      final bytes = await File(pickedFile.path).readAsBytes();
      setState(() {
        _imageFile = File(pickedFile.path);
        base64Image = base64Encode(bytes);
      });
    }
  }

  Future<void> saveProfile() async {
    setState(() => isSaving = true);
    
    DriverProfile updatedProfile = DriverProfile(
      fullName: nameController.text.trim(),
      phoneNumber: phoneController.text.trim(),
      vehicleNumber: vehicleController.text.trim(),
      profileImageBase64: base64Image,
    );

    bool success = await driverService.updateDriverProfile(widget.driverId, updatedProfile);

    if (mounted) {
      setState(() => isSaving = false);
      if (success) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Profile updated successfully!')));
        Navigator.pop(context, true); // Return true to indicate change
      } else {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('Failed to update profile.')));
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text('Edit Profile', style: TextStyle(color: Colors.black)),
        backgroundColor: Colors.transparent,
        elevation: 0,
        iconTheme: const IconThemeData(color: Colors.black),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          children: [
            Center(
              child: GestureDetector(
                onTap: _pickImage,
                child: Stack(
                  children: [
                    CircleAvatar(
                      radius: 50,
                      backgroundColor: const Color(0xFFE8F4F8),
                      backgroundImage: _imageFile != null 
                          ? FileImage(_imageFile!) 
                          : (base64Image != null && base64Image!.isNotEmpty)
                              ? MemoryImage(base64Decode(base64Image!)) as ImageProvider
                              : null,
                      child: _imageFile == null && (base64Image == null || base64Image!.isNotEmpty == false)
                          ? const Icon(Icons.person, size: 50, color: Color(0xFF00C2E0))
                          : null,
                    ),
                    Positioned(
                      bottom: 0,
                      right: 0,
                      child: Container(
                        padding: const EdgeInsets.all(6),
                        decoration: BoxDecoration(
                          color: const Color(0xFF00C2E0),
                          shape: BoxShape.circle,
                          border: Border.all(color: Colors.white, width: 2),
                        ),
                        child: const Icon(Icons.camera_alt, color: Colors.white, size: 18),
                      ),
                    )
                  ],
                ),
              ),
            ),
            const SizedBox(height: 30),
            TextField(
              controller: nameController,
              decoration: const InputDecoration(
                labelText: 'Full Name',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.person_outline),
              ),
            ),
            const SizedBox(height: 20),
            TextField(
              controller: phoneController,
              decoration: const InputDecoration(
                labelText: 'Mobile Number',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.phone_outlined),
              ),
              keyboardType: TextInputType.phone,
            ),
            const SizedBox(height: 20),
            TextField(
              controller: vehicleController,
              decoration: const InputDecoration(
                labelText: 'Vehicle Number',
                border: OutlineInputBorder(),
                prefixIcon: Icon(Icons.directions_bus_outlined),
              ),
            ),
            const SizedBox(height: 40),
            SizedBox(
              width: double.infinity,
              height: 55,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF00C2E0),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
                onPressed: isSaving ? null : saveProfile,
                child: isSaving
                    ? const CircularProgressIndicator(color: Colors.white)
                    : const Text(
                        'Save Changes',
                        style: TextStyle(
                          fontSize: 18, 
                          fontWeight: FontWeight.bold, 
                          color: Colors.white
                        ),
                      ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
