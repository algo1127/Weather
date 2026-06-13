package com.algo1127.weather.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.algo1127.weather.R

class HourlyAdapter(private val items: List<HourlyForecastItem>) :
    RecyclerView.Adapter<HourlyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHour: TextView = view.findViewById(R.id.tvHour)
        val tvHourTemp: TextView = view.findViewById(R.id.tvHourTemp)
        val lottieHourlyIcon: LottieAnimationView = view.findViewById(R.id.lottieHourlyIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvHour.text = item.time
        holder.tvHourTemp.text = "${item.temp}°"

        // Map and play Lottie animation
        val iconRes = AemetIconMapper.getWeatherIcon(item.aemetCode, item.description)
        holder.lottieHourlyIcon.setAnimation(iconRes)
        holder.lottieHourlyIcon.playAnimation()
    }

    override fun getItemCount() = items.size
}