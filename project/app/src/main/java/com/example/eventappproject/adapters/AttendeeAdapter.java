package com.example.eventappproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventappproject.R;
import com.example.eventappproject.interfaces.UserCreatedEventRecyclerViewInterface;
import com.example.eventappproject.models.Event;
import com.example.eventappproject.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class AttendeeAdapter extends RecyclerView.Adapter<AttendeeAdapter.ViewHolder> {
    private Event event;
    private List<String> attendees;
    private UserCreatedEventRecyclerViewInterface userCreatedEventInterface;
    private DatabaseReference dbReferenceUsers;

    public AttendeeAdapter(Event event, List<String> attendees, UserCreatedEventRecyclerViewInterface userCreatedEventInterface) {
        this.event = event;
        this.attendees = attendees;
        this.userCreatedEventInterface = userCreatedEventInterface;

        this.dbReferenceUsers = FirebaseDatabase.getInstance("https://eventapp-18029-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Users");
    }

    // This method is responsible for inflating layout (Giving a look to every row)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View eventView = inflater.inflate(R.layout.attendee_item, parent, false);

        AttendeeAdapter.ViewHolder viewHolder = new AttendeeAdapter.ViewHolder(eventView);
        return viewHolder;
    }

    // This method assigns values to each of the role based on the position in the recycler
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String userID = attendees.get(position);

        dbReferenceUsers.child(userID).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                User user = task.getResult().getValue(User.class);
                holder.attendeeName.setText(user.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return attendees.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView attendeeName;
        private ImageButton removeAttendeeBTN;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            attendeeName = itemView.findViewById(R.id.attendeeNameTV);
            removeAttendeeBTN = itemView.findViewById(R.id.removeAttendeeBTN);

            removeAttendeeBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    userCreatedEventInterface.onAttendeeRemoved(event, attendees.get(position));
                    attendees.remove(position);
                    notifyDataSetChanged();
                }
            });
        }
    }
}
