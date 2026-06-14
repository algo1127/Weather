package com.algo1127.weather

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.ComposeShader
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.abs
import java.util.Calendar

class SkyBackgroundView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        isDither = true
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
    }

    // Slow time counter for gradient animation
    private var time = 0f
    // Faster time counter for glow/shimmer effects
    private var fastTime = 0f

    private var weatherCondition = WeatherCondition.SUNNY

    // Hour of day (0–23), used to modulate golden hour / night
    private var hourOfDay: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    enum class WeatherCondition {
        SUNNY, CLOUDY, RAINY, STORMY, SNOWY, FOGGY, NIGHT
    }

    fun setWeatherCondition(condition: WeatherCondition) {
        weatherCondition = condition
        invalidate()
    }

    /** Optionally inject a specific hour (for testing or manual time-of-day). */
    fun setHourOfDay(hour: Int) {
        hourOfDay = hour.coerceIn(0, 23)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        // Advance time counters
        time += 0.002f
        fastTime += 0.012f
        if (time > 1000f) time -= 1000f
        if (fastTime > 1000f) fastTime -= 1000f

        // Determine golden hour influence (sunrise ~6–8, sunset ~18–20)
        val goldenHourStrength = when (hourOfDay) {
            6 -> 0.6f
            7 -> 1.0f
            8 -> 0.5f
            18 -> 0.5f
            19 -> 1.0f
            20 -> 0.6f
            else -> 0f
        } * (1f + sin(fastTime * 0.5f) * 0.08f) // subtle pulse

        drawLayeredGradient(canvas, w, h, goldenHourStrength)
        drawAtmosphericGlow(canvas, w, h, goldenHourStrength)

        postInvalidateOnAnimation()
    }

    // ─── Main layered gradient ────────────────────────────────────────────────

    private fun drawLayeredGradient(canvas: Canvas, w: Float, h: Float, golden: Float) {

        // Wave offsets — two independent sine waves for organic motion
        val wave1 = sin(time * 0.4f)
        val wave2 = sin(time * 0.27f + 1.3f)
        val blend = (wave1 * 0.6f + wave2 * 0.4f).coerceIn(-1f, 1f)
        val t = blend * 0.5f + 0.5f          // normalised 0–1
        val vertShift = sin(time * 0.18f) * h * 0.025f

        val (colors, positions) = buildGradient(t, golden, h)

        val shader = LinearGradient(
            0f, vertShift,
            0f, h + vertShift,
            colors.toIntArray(),
            positions.toFloatArray(),
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
        canvas.drawRect(0f, 0f, w, h, paint)
        paint.shader = null
    }

    /**
     * Returns (colors, positions) for the multi-stop gradient appropriate to
     * the current weather condition and golden-hour strength.
     */
    private fun buildGradient(t: Float, golden: Float, h: Float): Pair<List<Int>, List<Float>> {
        return when (weatherCondition) {

            WeatherCondition.SUNNY -> {
                // Zenith deep blue → mid sky azure → horizon haze
                val zenith = lerpColor("#1A6EC2", "#3A85D5", t)
                val midSky = lerpColor("#4A9FEA", "#5AAFF5", t)
                val horizon = lerpColor("#89C8F8", "#A8D8FF", t)
                val haze = lerpColor("#C2E5FF", "#D5EEFF", t)

                // Inject golden warm tone near horizon during golden hour
                val horizonBlended = blendColors(horizon, Color.parseColor("#FFD580"), golden * 0.55f)
                val hazeBlended = blendColors(haze, Color.parseColor("#FFBC6A"), golden * 0.45f)

                listOf(zenith, midSky, horizonBlended, hazeBlended) to
                        listOf(0f, 0.38f, 0.72f, 1f)
            }

            WeatherCondition.CLOUDY -> {
                val top = lerpColor("#7A8593", "#8A95A2", t)
                val mid = lerpColor("#A8B0BA", "#B0B8C2", t)
                val low = lerpColor("#C8CDD4", "#D0D5DB", t)
                val base = lerpColor("#DDE1E7", "#E5E9EE", t)
                listOf(top, mid, low, base) to listOf(0f, 0.3f, 0.65f, 1f)
            }

            WeatherCondition.RAINY -> {
                val top = lerpColor("#3D4D5E", "#485868", t)
                val mid = lerpColor("#5A6A7A", "#637280", t)
                val low = lerpColor("#6E7E8A", "#788692", t)
                val base = lerpColor("#8A949E", "#92A0AA", t)
                listOf(top, mid, low, base) to listOf(0f, 0.28f, 0.6f, 1f)
            }

            WeatherCondition.STORMY -> {
                // High contrast; dramatic pulse makes top nearly black
                val pulse = (sin(fastTime * 1.2f) * 0.12f + 0.5f).coerceIn(0f, 1f)
                val top = lerpColor("#1C222A", "#2A3340", pulse)
                val mid = lerpColor("#2E3740", "#38424C", t)
                val low = lerpColor("#454E58", "#505862", t)
                val base = lerpColor("#5C6570", "#686E78", t)
                listOf(top, mid, low, base) to listOf(0f, 0.25f, 0.58f, 1f)
            }

            WeatherCondition.SNOWY -> {
                val top = lerpColor("#8AAAC8", "#96B4D0", t)
                val mid = lerpColor("#B2CCE0", "#BDD4E7", t)
                val low = lerpColor("#D5E8F2", "#DCEEf7", t)
                val base = lerpColor("#EAF5FA", "#F2F9FD", t)
                listOf(top, mid, low, base) to listOf(0f, 0.32f, 0.65f, 1f)
            }

            WeatherCondition.FOGGY -> {
                // Almost monochromatic, breathes very slowly
                val breath = sin(time * 0.08f) * 0.5f + 0.5f
                val top = lerpColor("#B0BBBF", "#B8C3C7", breath)
                val mid = lerpColor("#C8D2D6", "#D0DADd", breath)
                val low = lerpColor("#DDE5E9", "#E5EDF0", breath)
                val base = lerpColor("#EEF4F6", "#F4F8FA", breath)
                listOf(top, mid, low, base) to listOf(0f, 0.3f, 0.62f, 1f)
            }

            WeatherCondition.NIGHT -> {
                // Deep indigo-navy; gentle starfield shimmer at top
                val shimmer = sin(fastTime * 0.6f) * 0.08f + 0.5f
                val zenith = lerpColor("#080E1A", "#0C1422", shimmer)
                val mid = lerpColor("#0F1A2E", "#132034", t)
                val low = lerpColor("#1A2840", "#1E3048", t)
                val base = lerpColor("#243650", "#2A3E5A", t)
                listOf(zenith, mid, low, base) to listOf(0f, 0.3f, 0.65f, 1f)
            }
        }
    }

    // ─── Atmospheric glow layer ───────────────────────────────────────────────

    /**
     * Draws a soft radial glow near the horizon — warm during golden hour,
     * cool-white for snow/fog, subtle purple for night.
     */
    private fun drawAtmosphericGlow(canvas: Canvas, w: Float, h: Float, golden: Float) {
        val cx = w * 0.5f
        // Glow position floats slightly up/down
        val cy = h * 0.78f + sin(time * 0.22f) * h * 0.015f
        val radius = w * 0.9f

        val (centerColor, edgeColor) = when (weatherCondition) {
            WeatherCondition.SUNNY -> {
                if (golden > 0.1f) {
                    val warm = blendColors(
                        Color.parseColor("#FFE08A"),
                        Color.parseColor("#FF9A3C"),
                        golden
                    )
                    Pair(setAlpha(warm, (golden * 140).toInt()), Color.TRANSPARENT)
                } else {
                    Pair(setAlpha(Color.parseColor("#C8E8FF"), 45), Color.TRANSPARENT)
                }
            }
            WeatherCondition.NIGHT ->
                Pair(setAlpha(Color.parseColor("#3040A0"), 55), Color.TRANSPARENT)
            WeatherCondition.SNOWY ->
                Pair(setAlpha(Color.parseColor("#DDEEFF"), 60), Color.TRANSPARENT)
            WeatherCondition.FOGGY ->
                Pair(setAlpha(Color.parseColor("#E8F0F5"), 80), Color.TRANSPARENT)
            WeatherCondition.STORMY ->
                Pair(setAlpha(Color.parseColor("#202832"), 100), Color.TRANSPARENT)
            else -> Pair(Color.TRANSPARENT, Color.TRANSPARENT)
        }

        if (Color.alpha(centerColor) == 0) return

        val radial = RadialGradient(
            cx, cy, radius,
            centerColor, edgeColor,
            Shader.TileMode.CLAMP
        )
        glowPaint.shader = radial
        canvas.drawCircle(cx, cy, radius, glowPaint)
        glowPaint.shader = null
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Linearly interpolate between two hex-string colors. */
    private fun lerpColor(hex1: String, hex2: String, t: Float): Int =
        blendColors(Color.parseColor(hex1), Color.parseColor(hex2), t.coerceIn(0f, 1f))

    /** Blend two ARGB ints by factor [0,1]. */
    private fun blendColors(c1: Int, c2: Int, t: Float): Int {
        val f = t.coerceIn(0f, 1f)
        val r = (Color.red(c1) + (Color.red(c2) - Color.red(c1)) * f).toInt()
        val g = (Color.green(c1) + (Color.green(c2) - Color.green(c1)) * f).toInt()
        val b = (Color.blue(c1) + (Color.blue(c2) - Color.blue(c1)) * f).toInt()
        val a = (Color.alpha(c1) + (Color.alpha(c2) - Color.alpha(c1)) * f).toInt()
        return Color.argb(a.coerceIn(0, 255), r.coerceIn(0, 255), g.coerceIn(0, 255), b.coerceIn(0, 255))
    }

    private fun setAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha.coerceIn(0, 255), Color.red(color), Color.green(color), Color.blue(color))
}