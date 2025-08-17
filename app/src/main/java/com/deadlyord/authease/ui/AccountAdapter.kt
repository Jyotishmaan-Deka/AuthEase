package com.deadlyord.authease.ui

import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.deadlyord.authease.auth.OTPGenerator
import com.deadlyord.authease.databinding.ItemAccountBinding
import com.deadlyord.authease.db.AccountEntity

class AccountAdapter(
    private val onDeleteClick: (AccountEntity) -> Unit
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

    inner class AccountViewHolder(
        private val binding: ItemAccountBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var countDownTimer: CountDownTimer? = null

        fun bind(account: AccountEntity) {
            binding.apply {
                textViewIssuer.text = account.issuer
                textViewAccountName.text = account.accountName

                updateOTP(account)
                startCountdown(account)

                buttonDelete.setOnClickListener {
                    onDeleteClick(account)
                }
            }
        }

        private fun updateOTP(account: AccountEntity) {
            val otp = OTPGenerator.generateTOTP(
                secret = account.secretKey,
                timeStep = account.period.toLong(),
                digits = account.digits,
                algorithm = account.algorithm
            )
            binding.textViewOtp.text = formatOTP(otp)
        }

        private fun formatOTP(otp: String): String {
            return if (otp.length == 6) {
                "${otp.substring(0, 3)} ${otp.substring(3)}"
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
                    binding.textViewTimer.text = seconds.toString()
                    binding.progressBar.progress = (seconds * 100 / account.period).toInt()
                }

                override fun onFinish() {
                    updateOTP(account)
                    startCountdown(account)
                }
            }
            countDownTimer?.start()
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
