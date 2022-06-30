package dev.tinelix.timers.modern.list_adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import dev.tinelix.timers.modern.R;
import dev.tinelix.timers.modern.activities.MainActivity;
import dev.tinelix.timers.modern.list_items.TemplateItem;

public class TemplateListAdapter extends RecyclerView.Adapter<TemplateListAdapter.Holder>{

    private ArrayList<TemplateItem> items = new ArrayList<>();
    private Context ctx;

    public TemplateListAdapter(Context context, ArrayList<TemplateItem> array) {
        ctx = context;
        items = array;
    }

    @Override
    public TemplateListAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(ctx).inflate(R.layout.templates_item, parent, false));
    }

    @Override
    public void onBindViewHolder(TemplateListAdapter.Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public Holder(View view) {
            super(view);
        }
        void bind(final int position) {
            Button template_chip = itemView.findViewById(R.id.template_chip);
            final TemplateItem item = items.get(position);
            template_chip.setText(item.fullName);
            template_chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ctx.getClass().getSimpleName().equals("MainActivity")) {
                        ((MainActivity) ctx).createTimerFromTemplate(item.name);
                    }
                }
            });
        }
    }
}
