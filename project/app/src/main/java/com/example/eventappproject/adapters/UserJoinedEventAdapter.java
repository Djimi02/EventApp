package com.example.eventappproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventappproject.R;
import com.example.eventappproject.interfaces.UserJoinedEventRecyclerViewInterface;
import com.example.eventappproject.models.Event;

import java.util.List;

public class UserJoinedEventAdapter extends RecyclerView.Adapter<UserJoinedEventAdapter.ViewHolder> {
    private List<Event> events;
    private UserJoinedEventRecyclerViewInterface userJoinedEventInterface;

    public UserJoinedEventAdapter(List<Event> events, UserJoinedEventRecyclerViewInterface userJoinedEventInterface) {
        this.events = events;
        this.userJoinedEventInterface = userJoinedEventInterface;
    }

    // This method is responsible for inflating layout (Giving a look to every row)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View eventView = inflater.inflate(R.layout.event_item_recycler , parent, false);

        UserJoinedEventAdapter.ViewHolder viewHolder = new UserJoinedEventAdapter.ViewHolder(eventView);
        return viewHolder;
    }

    // This method assigns values to each of the role based on the position in the recycler
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = events.get(position);
        holder.eventName.setText(event.getName());
        holder.eventDate.setText(event.getDate() + " " + event.getTime());

//        switch (event.getCategory()) {
//            case "Party":
//                holder.icon.setImageResource(R.drawable.logo_category_party);
//                break;
//            case "Sport":
//                holder.icon.setImageResource(R.drawable.logo_category_sport);
//                break;
//            case "Culture":
//                holder.icon.setImageResource(R.drawable.logo_category_culture);
//                break;
//            case "Food":
//                holder.icon.setImageResource(R.drawable.logo_category_food);
//                break;
//            case "Drinks":
//                holder.icon.setImageResource(R.drawable.logo_category_drinks);
//                break;
//            case "Other":
//                holder.icon.setImageResource(R.drawable.logo_category_other);
//                break;
//            default:
//                holder.icon.setImageResource(R.drawable.baseline_question_mark_24);
//        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView eventName;
        private TextView eventDate;
        private ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.eventNameText);
            eventDate = itemView.findViewById(R.id.eventDateTime);
            icon = itemView.findViewById(R.id.iconImageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    if (userJoinedEventInterface != null && position != RecyclerView.NO_POSITION) {
                        userJoinedEventInterface.onJoinedEventItemClick(position);
                    }
                }
            });
        }
    }
}
