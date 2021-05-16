package com.geekbrains.weatherwithmvvm.framework.ui.details

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.geekbrains.weatherwithmvvm.*
import com.geekbrains.weatherwithmvvm.databinding.FragmentDetailsBinding
import com.geekbrains.weatherwithmvvm.framework.services.DetailsService
import com.geekbrains.weatherwithmvvm.framework.services.LATITUDE_EXTRA
import com.geekbrains.weatherwithmvvm.framework.services.LONGITUDE_EXTRA
import com.geekbrains.weatherwithmvvm.model.AppState
import com.geekbrains.weatherwithmvvm.model.entities.Weather
import com.geekbrains.weatherwithmvvm.framework.ui.main.MainViewModel
import com.geekbrains.weatherwithmvvm.model.WeatherLoader
import com.geekbrains.weatherwithmvvm.model.rest_entities.FactDTO
import com.geekbrains.weatherwithmvvm.model.rest_entities.WeatherDTO
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_details.*

@Suppress("NAME_SHADOWING")
class DetailsFragment : Fragment() {
    private lateinit var binding: FragmentDetailsBinding

    private lateinit var weather: Weather

    private val onLoadListener: WeatherLoader.WeatherLoaderListener =
        object : WeatherLoader.WeatherLoaderListener {
            override fun onLoaded(weatherDTO: WeatherDTO) {
                displayWeather(weatherDTO)
            }

            override fun onFailed(throwable: Throwable) {
                Log.e(
                    "onLoadListener.OnFailed",
                    getString(R.string.server_connection_error), throwable
                )
                throwable.printStackTrace()
                Snackbar.make(
                    binding.mainView,
                    getString(R.string.server_connection_error),
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
        }
    private val viewModel: DetailsViewModel by lazy {
        ViewModelProvider(this).get(DetailsViewModel::class.java)
    }


    private val loadResultsReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getStringExtra(DETAILS_LOAD_RESULT_EXTRA)) {
                DETAILS_INTENT_EMPTY_EXTRA -> TODO(PROCESS_ERROR)
                DETAILS_DATA_EMPTY_EXTRA -> TODO(PROCESS_ERROR)
                DETAILS_RESPONSE_EMPTY_EXTRA -> TODO(PROCESS_ERROR)
                DETAILS_REQUEST_ERROR_EXTRA -> TODO(PROCESS_ERROR)
                DETAILS_REQUEST_ERROR_MESSAGE_EXTRA -> TODO(PROCESS_ERROR)
                DETAILS_URL_MALFORMED_EXTRA -> TODO(PROCESS_ERROR)
                DETAILS_RESPONSE_SUCCESS_EXTRA -> renderData(
                    WeatherDTO(
                        FactDTO(
                            intent.getIntExtra(
                                DETAILS_TEMP_EXTRA, TEMP_INVALID
                            ),
                            intent.getIntExtra(DETAILS_FEELS_LIKE_EXTRA, FEELS_LIKE_INVALID),
                            intent.getStringExtra(
                                DETAILS_CONDITION_EXTRA
                            )
                        )
                    )
                )
                else -> TODO(PROCESS_ERROR)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            LocalBroadcastManager.getInstance(it).registerReceiver(
                loadResultsReceiver,
                IntentFilter(DETAILS_INTENT_FILTER)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        weather = arguments?.getParcelable(BUNDLE_EXTRA) ?: Weather()
        getWeather()

        val intent = Intent(requireContext(), DetailsService::class.java).apply {
            putExtra(
                LATITUDE_EXTRA,
                weather.city.lat
            )
            putExtra(
                LONGITUDE_EXTRA,
                weather.city.lon
            )
        }
        DetailsService.start(requireContext(), intent)
    }

    private fun getWeather() {
        with(binding) {
            mainView.visibility = View.GONE
            loadingLayout.visibility = View.VISIBLE
        }
        context?.let {
            it.startService(Intent(it, DetailsService::class.java).apply {
                putExtra(LATITUDE_EXTRA, weather.city.lat)
                putExtra(LONGITUDE_EXTRA, weather.city.lon)
            })
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(loadResultsReceiver, IntentFilter(DETAILS_INTENT_FILTER))
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(loadResultsReceiver)
        super.onStop()
    }

    private fun renderData(weatherDTO: WeatherDTO) = with(binding) {
        mainView.visibility = View.VISIBLE
        loadingLayout.visibility = View.GONE

        val fact = weatherDTO.fact
        val temp = fact!!.temp
        val feelsLike = fact.feels_like
        val condition = fact.condition
        if (temp == TEMP_INVALID || feelsLike == FEELS_LIKE_INVALID || condition == null) {
            TODO(PROCESS_ERROR)
        } else {
            val city = weather.city
            cityName.text = city.city
            cityCoordinates.text = String.format(
                getString(R.string.city_coordinates),
                city.lat.toString(),
                city.lon.toString()
            )
            temperatureValue.text = temp.toString()
            feelsLikeValue.text = feelsLike.toString()
            weatherCondition.text = condition
        }
    }

    private fun displayWeather(weatherDTO: WeatherDTO) {
        with(binding) {
            mainView.visibility = View.VISIBLE
            loadingLayout.visibility = View.GONE
            val city = weather.city
            cityName.text = city.city
            cityCoordinates.text = String.format(
                getString(R.string.city_coordinates),
                city.lat.toString(), city.lon.toString()
            )
            weatherCondition.text = weatherDTO.fact?.condition
            temperatureValue.text = weatherDTO.fact?.temp.toString()
            feelsLikeValue.text = weatherDTO.fact?.feels_like.toString()
        }
    }

    companion object {
        const val BUNDLE_EXTRA = "weather"

        fun newInstance(bundle: Bundle): DetailsFragment {
            val fragment = DetailsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun onDestroy() {
        context?.let {
            LocalBroadcastManager.getInstance(it).unregisterReceiver(loadResultsReceiver)
        }
        super.onDestroy()
    }
}

