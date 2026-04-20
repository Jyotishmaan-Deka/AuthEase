package com.deadlyord.authease.ui

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.deadlyord.authease.R
import com.deadlyord.authease.auth.SecureOTPHelper
import com.deadlyord.authease.auth.TOTP
import com.deadlyord.authease.databinding.ItemAccountBinding
import com.deadlyord.authease.db.AccountEntity
import com.deadlyord.authease.utils.OtpFormatter

class AccountAdapter(
    private val onDeleteClick: (AccountEntity) -> Unit,
    private val onCopyClick: (String) -> Unit = {}
) : ListAdapter<AccountEntity, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

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

        fun bind(account: AccountEntity) {
            val context = binding.root.context

            // FIX 1: Use SecureOTPHelper to get the decrypted secret (prevents OTP generation
            // from using the encrypted ciphertext directly as the TOTP seed)
            val secureOTPHelper = SecureOTPHelper(context)
            val decryptedSecret = secureOTPHelper.getDecryptedSecret(account)

            // FIX 2: Create a TOTP instance and use its methods (was previously unused)
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

                // Avatar initial with deterministic color
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
            }
        }

        private fun updateOTP(totp: TOTP) {
            currentOTP = totp.generateCurrentCode()  // Now using TOTP class
            binding.textViewOtp.text = formatOTP(currentOTP)
        }

        // Fix 5: Delegate to shared OtpFormatter — no more duplication
        private fun formatOTP(otp: String): String = OtpFormatter.format(otp)

        private fun startCountdown(totp: TOTP) {
            countDownTimer?.cancel()
            val remainingTime = totp.getRemainingTime()  // Now using TOTP class

            countDownTimer = object : CountDownTimer(remainingTime * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val seconds = millisUntilFinished / 1000
                    binding.textViewTimer.text = "${seconds}s"
                    binding.progressBarTimer.progress = (seconds * 100 / totp.period).toInt()

                    val tintColor = if (seconds <= 5)
                        ContextCompat.getColor(binding.root.context, R.color.timer_urgent)
                    else
                        ContextCompat.getColor(binding.root.context, R.color.timer_normal)
                    binding.progressBarTimer.progressTintList =
                        android.content.res.ColorStateList.valueOf(tintColor)
                    binding.textViewTimer.setTextColor(tintColor)
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
