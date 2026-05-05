package com.armunstudio.authease.ui

import android.annotation.SuppressLint
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.armunstudio.authease.R
import com.armunstudio.authease.auth.SecureOTPHelper
import com.armunstudio.authease.auth.TOTP
import com.armunstudio.authease.databinding.ItemAccountBinding
import com.armunstudio.authease.db.AccountEntity
import com.armunstudio.authease.utils.OtpFormatter
import java.util.Collections

class AccountAdapter(
    private val onDeleteClick: (AccountEntity) -> Unit,
    private val onCopyClick: (String) -> Unit = {},
    private val onReorderComplete: (List<AccountEntity>) -> Unit = {}
) : ListAdapter<AccountEntity, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

/*     JD :  Drag-to-reorder state
     Separate mutable list used while a drag is in progress so we can call
     notifyItemMoved without conflicting with DiffUtil's internal list.*/
    private val dragList = mutableListOf<AccountEntity>()
    private var isDragging = false

    // JD : Injected by HomeFragment after ItemTouchHelper is created
    var itemTouchHelper: ItemTouchHelper? = null

    fun startDrag() {
        isDragging = true
        dragList.clear()
        dragList.addAll(currentList)
    }

    /** Called by ItemTouchHelper.Callback.onMove — swaps items and triggers visual move. */
    fun moveItem(from: Int, to: Int): Boolean {
        if (!isDragging) return false
        if (from < to) {
            for (i in from until to) Collections.swap(dragList, i, i + 1)
        } else {
            for (i in from downTo to + 1) Collections.swap(dragList, i, i - 1)
        }
        notifyItemMoved(from, to)
        return true
    }

    /** Called by ItemTouchHelper.Callback.clearView — drag finished, persist new order. */
    fun endDrag() {
        isDragging = false
        onReorderComplete(dragList.toList())
    }

    // JD : Override so ViewHolder.bind reads from dragList during an active drag
    override fun getItem(position: Int): AccountEntity =
        if (isDragging) dragList[position] else super.getItem(position)

    override fun getItemCount(): Int =
        if (isDragging) dragList.size else super.getItemCount()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: AccountViewHolder) {
        super.onViewRecycled(holder)
        holder.cleanup()
    }

    inner class AccountViewHolder(
        private val binding: ItemAccountBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var countDownTimer: CountDownTimer? = null
        private var currentOTP: String = ""

        @SuppressLint("ClickableViewAccessibility")
        fun bind(account: AccountEntity) {
            val context = binding.root.context

            val secureOTPHelper = SecureOTPHelper(context)
            val decryptedSecret = secureOTPHelper.getDecryptedSecret(account)

            val totp = TOTP(
                issuer = account.issuer,
                accountName = account.accountName,
                secret = decryptedSecret,
                algorithm = account.algorithm,
                digits = account.digits,
                period = account.period
            )

            binding.apply {
                textViewIssuer.text = account.issuer.ifEmpty { "Unknown" }
                textViewAccountName.text = account.accountName

                val initial = account.issuer.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                textViewAvatar.text = initial
                val avatarColors = listOf(
                    R.color.avatar_blue, R.color.avatar_teal, R.color.avatar_indigo,
                    R.color.avatar_purple, R.color.avatar_green, R.color.avatar_orange
                )
                val colorRes = avatarColors[account.issuer.hashCode().and(0x7FFFFFFF) % avatarColors.size]
                textViewAvatar.backgroundTintList = ContextCompat.getColorStateList(context, colorRes)

                updateOTP(totp)
                startCountdown(totp)

                buttonDelete.setOnClickListener { onDeleteClick(account) }
                buttonCopy.setOnClickListener {
                    if (currentOTP.isNotEmpty()) onCopyClick(OtpFormatter.stripSpaces(currentOTP))
                }
                root.setOnClickListener {
                    if (currentOTP.isNotEmpty()) onCopyClick(OtpFormatter.stripSpaces(currentOTP))
                }

                // JD : Drag handle, touch-down starts the drag immediately (no long press needed)
                dragHandle.setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        startDrag()
                        itemTouchHelper?.startDrag(this@AccountViewHolder)
                    }
                    false
                }
            }
        }

        private fun updateOTP(totp: TOTP) {
            currentOTP = totp.generateCurrentCode()
            binding.textViewOtp.text = OtpFormatter.format(currentOTP)
        }

        private fun startCountdown(totp: TOTP) {
            countDownTimer?.cancel()
            val remainingTime = totp.getRemainingTime()

            countDownTimer = object : CountDownTimer(remainingTime * 1000L, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val seconds = millisUntilFinished / 1000
                    binding.textViewTimer.text = "${seconds}s"
                    binding.progressBarTimer.progress = (seconds * 100 / totp.period).toInt()

                    val isUrgent = seconds <= 5
                    val tintColor = if (isUrgent)
                        ContextCompat.getColor(binding.root.context, R.color.timer_urgent)
                    else
                        ContextCompat.getColor(binding.root.context, R.color.timer_normal)

                    binding.progressBarTimer.progressTintList =
                        android.content.res.ColorStateList.valueOf(tintColor)
                    binding.textViewTimer.setTextColor(tintColor)

                    // JD : Swap timer chip background to red color when urgent
                    val bgRes = if (isUrgent) R.drawable.bg_delete_circle else R.drawable.bg_timer_chip
                    binding.textViewTimer.setBackgroundResource(bgRes)
                }

                override fun onFinish() {
                    updateOTP(totp)
                    startCountdown(totp)
                }
            }.start()
        }

        fun cleanup() { countDownTimer?.cancel() }
    }

    class AccountDiffCallback : DiffUtil.ItemCallback<AccountEntity>() {
        override fun areItemsTheSame(oldItem: AccountEntity, newItem: AccountEntity) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: AccountEntity, newItem: AccountEntity) =
            oldItem == newItem
    }
}