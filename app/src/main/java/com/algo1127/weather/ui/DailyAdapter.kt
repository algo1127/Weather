package com.algo1127.weather.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.algo1127.weather.R

class DailyAdapter(private val items: List<DailyForecastItem>) :
    RecyclerView.Adapter<DailyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDayName: TextView = view.findViewById(R.id.tvDayName)
        val tvDaySky: TextView = view.findViewById(R.id.tvDaySky)
        val tvDayRain: TextView = view.findViewById(R.id.tvDayRain)
        val lottieRaindropIcon: LottieAnimationView = view.findViewById(R.id.lottieRaindropIcon)
        val tvDayMin: TextView = view.findViewById(R.id.tvDayMin)
        val tvDayMax: TextView = view.findViewById(R.id.tvDayMax)
        val lottieDailyIcon: LottieAnimationView = view.findViewById(R.id.lottieDailyIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvDayName.text = item.dayName
        holder.tvDaySky.text = item.skyDescription
        
        if (item.rainProbability > 0) {
            holder.tvDayRain.text = "${item.rainProbability}%"
            holder.lottieRaindropIcon.visibility = View.VISIBLE
            holder.lottieRaindropIcon.playAnimation()
        } else {
            holder.tvDayRain.text = ""
            holder.lottieRaindropIcon.visibility = View.GONE
        }

        holder.tvDayMin.text = "${item.minTemp}°"
        holder.tvDayMax.text = "${item.maxTemp}°"

        // Map and play Lottie animation
        val iconRes = AemetIconMapper.getWeatherIcon(item.aemetCode, item.skyDescription)
        holder.lottieDailyIcon.setAnimation(iconRes)
        holder.lottieDailyIcon.playAnimation()
    }

    override fun getItemCount() = items.size
}