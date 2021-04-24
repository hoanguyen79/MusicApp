package com.music.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.music.R;
import com.music.databinding.FragmentAccountBinding;
import com.music.ui.home.adapters.song.SongChartVerticalAdapter;
import com.music.ui.home.adapters.song.SongChartVerticalItemDecoration;

import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AccountFragment extends Fragment {
    @Nullable
    private FragmentAccountBinding binding;

    @NonNull
    public FragmentAccountBinding getBinding() {
        return Objects.requireNonNull(binding);
    }

    @SuppressWarnings({"NotNullFieldNotInitialized", "FieldCanBeLocal"})
    @NonNull
    private AccountViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);

        setHasOptionsMenu(true);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        FirebaseUser user = viewModel.getCurrentUser();

        Glide.with(this)
                .load(user.getPhotoUrl())
                .circleCrop()
                .fallback(R.drawable.purple_gradient_background)
                .into(getBinding().ivUserAvatar);

        getBinding().tvHelloUser.setText(user.getDisplayName());

        final LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);


        getBinding().rvHistory.setHasFixedSize(true);
        getBinding().rvHistory.setLayoutManager(linearLayoutManager);
        getBinding().rvHistory.addItemDecoration(new SongChartVerticalItemDecoration());

        viewModel.getHistories().observe(getViewLifecycleOwner(), response -> {
            switch (response.status) {
                case SUCCESS:
                    getBinding().rvHistory.setAdapter(new SongChartVerticalAdapter(Objects.requireNonNull(response.data)));
                    getBinding().prbLoading.setVisibility(View.GONE);
                    break;
                case LOADING:
                    getBinding().prbLoading.setVisibility(View.VISIBLE);
                    break;
                case ERROR:
                    getBinding().prbLoading.setVisibility(View.GONE);
                    break;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.setting_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_setting) {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.navigation_setting_fragment);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}