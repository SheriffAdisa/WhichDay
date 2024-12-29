package uk.ac.aston.cs3mdd.whichdayapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

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
    holder.forecastTextView.setText(daySummary.toString());
  }


  @Override
  public int getItemCount() {
    return forecastList.size();
  }

  static class ForecastViewHolder extends RecyclerView.ViewHolder {
    final TextView forecastTextView;

    public ForecastViewHolder(@NonNull View itemView) {
      super(itemView);
      forecastTextView = itemView.findViewById(R.id.forecastTextView);
    }
  }
}
