package com.music.ui.home.adapters.song;

import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SongChartVerticalItemDecoration extends RecyclerView.ItemDecoration {
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final int position = parent.getChildAdapterPosition(view);

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        if (position == 0) {
            outRect.left = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                    parent.getContext().getResources().getDisplayMetrics());
        }

        outRect.right = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                parent.getContext().getResources().getDisplayMetrics());
    }
}
