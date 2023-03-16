package com.github.keithyokoma.poc.bottomnavdeeplink.ui.home

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDeepLinkBuilder
import com.github.keithyokoma.poc.bottomnavdeeplink.MainActivity
import com.github.keithyokoma.poc.bottomnavdeeplink.R
import com.github.keithyokoma.poc.bottomnavdeeplink.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showNotification()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        binding.sendNotification.setOnClickListener {
            showNotification()
        }
        return root
    }

    private fun showNotification() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        val manager = NotificationManagerCompat.from(requireContext())
        if (manager.getNotificationChannel(NOTIFICATION_CHANNEL) == null) {
            manager.createNotificationChannel(
                NotificationChannelCompat.Builder(NOTIFICATION_CHANNEL, NotificationManagerCompat.IMPORTANCE_HIGH)
                    .setName("sample notification")
                    .setDescription("sample notification")
                    .build()
            )
        }
        val contentIntent = NavDeepLinkBuilder(requireContext())
            .setGraph(R.navigation.mobile_navigation)
            .addDestination(R.id.navigation_notifications)
            .setComponentName(ComponentName(requireContext().packageName, MainActivity::class.java.name))
            .createPendingIntent()
        val notification = NotificationCompat.Builder(requireContext(), NOTIFICATION_CHANNEL)
            .setContentTitle("test")
            .setContentText("click to see all notifications")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setContentIntent(contentIntent)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notificationPermissionLauncher.unregister()
        _binding = null
    }

    companion object {
        private const val NOTIFICATION_ID = 234
        private const val NOTIFICATION_CHANNEL = "sample_channel"
    }
}