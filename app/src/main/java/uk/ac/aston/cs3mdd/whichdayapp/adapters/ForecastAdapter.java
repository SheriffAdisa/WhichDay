package uk.ac.aston.cs3mdd.whichdayapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import uk.ac.aston.cs3mdd.whichdayapp.R;
import uk.ac.aston.cs3mdd.whichdayapp.models.DaySummary;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

  private final List<DaySummary> forecastList;

  public ForecastAdapter(List<DaySummary> forecastList) {
    this.forecastList = forecastList;
  }

  @NonNull
  @Override
  public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.forecast_item, parent, false);
    return new ForecastViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
    DaySummary daySummary = forecastList.get(position);

    holder.forecastDate.setText(formatDate(daySummary.getDate()));
    holder.forecastTemp.setText(String.format("%.1f°C", daySummary.getAvgTemp()));
    holder.forecastDescription.setText(daySummary.getDescription());

    holder.weatherIcon.setImageResource(getWeatherIcon(daySummary.getDescription()));
  }

  @Override
  public int getItemCount() {
    return forecastList.size();
  }

  private int getWeatherIcon(String description) {
    if (description == null) return R.drawable.ic_unknown;

    String desc = description.toLowerCase();
    if (desc.contains("rain")) {
      return R.drawable.ic_rain;
    } else if (desc.contains("sun")) {
      return R.drawable.ic_sunny;
    } else if (desc.contains("cloud")) {
      return R.drawable.ic_cloud;
    } else if (desc.contains("snow")) {
      return R.drawable.ic_snow;
    } else if (desc.contains("storm")) {
      return R.drawable.ic_thunderstorm;
    } else if (desc.contains("mist")) {
      return R.drawable.ic_mist;
    }

    return R.drawable.ic_unknown;
  }

  private String formatDate(String date) {
    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    try {
      return outputFormat.format(inputFormat.parse(date));
    } catch (ParseException e) {
      e.printStackTrace();
      return date;
    }
  }

  static class ForecastViewHolder extends RecyclerView.ViewHolder {
    final TextView forecastDate;
    final ImageView weatherIcon;
    final TextView forecastTemp;
    final TextView forecastDescription;

    public ForecastViewHolder(@NonNull View itemView) {
      super(itemView);
      forecastDate = itemView.findViewById(R.id.forecastDate);
      weatherIcon = itemView.findViewById(R.id.weatherIcon);
      forecastTemp = itemView.findViewById(R.id.forecastTemp);
      forecastDescription = itemView.findViewById(R.id.forecastDescription);
    }
  }
}
