package com.example.mybudget.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.mybudget.data.repository.BillRepository
import com.example.mybudget.data.repository.DebtRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val billRepository: BillRepository,
    private val debtRepository: DebtRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val bills = billRepository.getAllBills().first()
            val debts = debtRepository.getAllDebts().first().filter { !it.isSettled }

            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            
            val todayDate = sdf.format(Calendar.getInstance().time)
            
            val calTmr = Calendar.getInstance()
            calTmr.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrowDate = sdf.format(calTmr.time)

            var upcomingCount = 0

            for (bill in bills) {
                val due = bill.dueDate.lowercase(Locale.getDefault())
                if (due.contains("today") || due.contains("tomorrow") || 
                    bill.dueDate.contains(todayDate) || bill.dueDate.contains(tomorrowDate)) {
                    upcomingCount++
                }
            }

            for (debt in debts) {
                val due = debt.dueDate.lowercase(Locale.getDefault())
                if (due.contains("today") || due.contains("tomorrow") || 
                    debt.dueDate.contains(todayDate) || debt.dueDate.contains(tomorrowDate)) {
                    upcomingCount++
                }
            }

            if (upcomingCount > 0) {
                showNotification(
                    title = "Upcoming Due Dates!",
                    message = "You have $upcomingCount bills or debts due soon."
                )
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // If Firebase is down or something, we can just return success or retry.
            // Returning success so it doesn't endlessly retry if auth is missing.
            Result.success()
        }
    }

    private fun showNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(context, "BILLS_DEBTS_CHANNEL")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return // No permission
            }
        }
        
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1001, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}
