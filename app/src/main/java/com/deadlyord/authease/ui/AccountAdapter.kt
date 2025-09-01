package com.deadlyord.authease.ui

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.deadlyord.authease.auth.OTPGenerator
import com.deadlyord.authease.auth.SecureOTPHelper
import com.deadlyord.authease.databinding.ItemAccountBinding
import com.deadlyord.authease.db.AccountEntity

class AccountAdapter(
    private val onDeleteClick: (AccountEntity) -> Unit,
    private val onCopyClick: (String) -> Unit = {}
) : ListAdapter<AccountEntity, AccountAdapter.AccountViewHolder>(AccountDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
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
            binding.apply {
                textViewIssuer.text = account.issuer.ifEmpty { "Unknown" }
                textViewAccountName.text = account.accountName

                updateOTP(account)
                startCountdown(account)

                buttonDelete.setOnClickListener {
                    onDeleteClick(account)
                }

                // Add click listener to copy OTP when tapped
                textViewOtp.setOnClickListener {
                    if (currentOTP.isNotEmpty()) {
                        onCopyClick(currentOTP.replace(" ", ""))
                    }
                }

                root.setOnClickListener {
                    if (currentOTP.isNotEmpty()) {
                        onCopyClick(currentOTP.replace(" ", ""))
                    }
                }
            }
        }

        private fun updateOTP(account: AccountEntity) {
            currentOTP = OTPGenerator.generateTOTP(
                secret = account.secretKey,
                timeStep = account.period.toLong(),
                digits = account.digits,
                algorithm = account.algorithm
            )
            binding.textViewOtp.text = formatOTP(currentOTP)
        }

        private fun formatOTP(otp: String): String {
            return if (otp.length == 6) {
                "${otp.substring(0, 3)} ${otp.substring(3)}"
            } else if (otp.length == 8) {
                "${otp.substring(0, 4)} ${otp.substring(4)}"
            } else {
                otp
            }
        }

        private fun startCountdown(account: AccountEntity) {
            countDownTimer?.cancel()

            val remainingTime = OTPGenerator.getRemainingTime(account.period.toLong())

            countDownTimer = object : CountDownTimer(remainingTime * 1000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val seconds = millisUntilFinished / 1000
                    binding.textViewTimer.text = "${seconds}s"
                    binding.progressBar.progress = (seconds * 100 / account.period).toInt()
                }

                override fun onFinish() {
                    updateOTP(account)
                    startCountdown(account)
                }
            }
            countDownTimer?.start()
        }

        fun cleanup() {
            countDownTimer?.cancel()
        }
    }

    class AccountDiffCallback : DiffUtil.ItemCallback<AccountEntity>() {
        override fun areItemsTheSame(oldItem: AccountEntity, newItem: AccountEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AccountEntity, newItem: AccountEntity): Boolean {
            return oldItem == newItem
        }
    }
}