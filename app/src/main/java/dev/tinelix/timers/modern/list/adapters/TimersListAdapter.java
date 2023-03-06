package dev.tinelix.timers.modern.list.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import dev.tinelix.timers.modern.R;
import dev.tinelix.timers.modern.core.ui.activities.MainActivity;
import dev.tinelix.timers.modern.list.items.TimerItem;

public class TimersListAdapter extends RecyclerView.Adapter<TimersListAdapter.Holder> {

    private ArrayList<TimerItem> items = new ArrayList<>();
    private Context ctx;

    public TimersListAdapter(Context context, ArrayList<TimerItem> array) {
        ctx = context;
        items = array;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new Holder(LayoutInflater.from(ctx).inflate(R.layout.timer_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    public TimerItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public final TextView item_title;
        public final TextView item_subtitle;
        public final TextView item_time_counter;
        public final ImageView item_icon;
        public final ImageButton edit_button;
        public final ImageButton delete_button;
        public final View convertView;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.item_title = view.findViewById(R.id.item_name);
            this.item_subtitle = view.findViewById(R.id.item_subtitle);
            this.item_time_counter = view.findViewById(R.id.remaining_time);
            this.item_icon = view.findViewById(R.id.icon);
            this.edit_button = view.findViewById(R.id.edit_button);
            this.delete_button = view.findViewById(R.id.delete_button);
        }

        void bind(final int position) {
            TimerItem item = getItem(position);
            item_title.setText(item.name);
            item_subtitle.setText(ctx.getResources().getString(R.string.time_at, new SimpleDateFormat("d MMMM yyyy").format(new Date(item.actionDate)),
                    new SimpleDateFormat("HH:mm").format(new Date(item.actionDate))));
            long diff = (new Date().getTime() - new Date(item.actionDate).getTime());
            long elapsed_sec = TimeUnit.SECONDS.convert(diff, TimeUnit.MILLISECONDS);
            long remaining_sec = TimeUnit.SECONDS.convert(-diff, TimeUnit.MILLISECONDS);
            updateTimeUI(item, diff, elapsed_sec, remaining_sec);

            edit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ctx.getClass().getSimpleName().equals("MainActivity")) {
                        ((MainActivity) ctx).showTimerEditorDialog(position);
                    }
                }
            });
            delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ctx.getClass().getSimpleName().equals("MainActivity")) {
                        ((MainActivity) ctx).deleteTimer(position);
                    }
                }
            });
        }

        private void updateTimeUI(final TimerItem item, long diff, long elapsed_sec, final long remaining_sec) {
            try {
                final long elapsed_days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                final long remaining_days = TimeUnit.DAYS.convert(-diff, TimeUnit.MILLISECONDS);
                final long elapsed_hours = TimeUnit.HOURS.convert(diff, TimeUnit.MILLISECONDS);
                final long remaining_hours = TimeUnit.HOURS.convert(-diff, TimeUnit.MILLISECONDS);
                Date timer_date = new Date();
                if (diff >= 0) {
                    timer_date = new Date(diff);
                } else {
                    timer_date = new Date(-diff);
                }
                if (item.action.equals("calculateRemainingTime")) {
                    if (remaining_sec > 0) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(timer_date);
                        item_time_counter.setText(ctx.getResources().getString(R.string.days_counter_remaining, remaining_days, (int) ((remaining_hours % 24)), timer_date.getMinutes(), timer_date.getSeconds()));
                    } else {
                        item_time_counter.setText(ctx.getResources().getString(R.string.days_counter_over));
                    }
                } else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(timer_date);
                    if (elapsed_days > 0) {
                        item_time_counter.setText(ctx.getResources().getString(R.string.days_counter_elapsed, elapsed_days, (int) ((elapsed_hours % 24)), timer_date.getMinutes(), timer_date.getSeconds()));
                    } else {
                        item_time_counter.setText(ctx.getResources().getString(R.string.days_counter_over));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
