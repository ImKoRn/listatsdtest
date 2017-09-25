package com.imkorn.listatsdtest.ui.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.imkorn.listatsdtest.R;
import com.imkorn.listatsdtest.databinding.DialogSelectAssetBinding;
import com.imkorn.listatsdtest.ui.adapters.AssetsAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by imkorn on 23.09.17.
 */

public class SelectAssetDialog extends DialogFragment {
    // Extras
    private static final String EXTRA_FOLDER = "EXTRA_FOLDER";

    // Keys
    private static final String KEY_ASSETS = "KEY_ASSETS";

    // Ui
    private DialogSelectAssetBinding binding;

    // Adapters
    private AssetsAdapter assetsAdapter;

    // Listeners
    private OnSelectAssetListener onSelectAssetListener;

    // Data
    private ArrayList<String> assets;
    private String folder;

    public static SelectAssetDialog newInstance(@NonNull String folder) {
        Bundle args = new Bundle();
        args.putString(EXTRA_FOLDER, folder);

        SelectAssetDialog fragment = new SelectAssetDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        folder = getArguments().getString(EXTRA_FOLDER,
                                          "");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Object pendingCallback = getParentFragment();
        if (pendingCallback == null) {
            pendingCallback = activity;
        }

        if (pendingCallback instanceof OnSelectAssetListener) {
            onSelectAssetListener = (OnSelectAssetListener) pendingCallback;
        } else {
            throw new ClassCastException(pendingCallback +
                                         " must implement " +
                                         OnSelectAssetListener.class.getName());
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_select_asset,
                                              container,
                                              false);
        binding = DialogSelectAssetBinding.bind(view);
        initUi(view);
        initCallbacks();
        return view;
    }

    private void initUi(View view) {
        final Activity activity = getActivity();
        binding.rvAssets.setLayoutManager(new LinearLayoutManager(activity));
        binding.rvAssets.setAdapter(assetsAdapter = new AssetsAdapter(activity));

        getDialog().setTitle(R.string.text_select_asset);
    }

    private void initCallbacks() {
        assetsAdapter.setListener(new AssetsAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v,
                                int position,
                                String assetName) {
                onSelectAssetListener.onSelect(SelectAssetDialog.this,
                                               assetName);
            }
        });
    }

    @Override
    public void onViewCreated(View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,
                            savedInstanceState);
        if (savedInstanceState == null) {
            try {
                final String[] list = getResources().getAssets()
                                                    .list(folder);
                assets = new ArrayList<>(list.length);
                Collections.addAll(assets, list);
            } catch (IOException e) {
                e.printStackTrace();
                assets = null;
            }
        } else {
            assets = savedInstanceState.getStringArrayList(KEY_ASSETS);
        }


        if (assets != null) {
            if (!assets.isEmpty()) {
                assetsAdapter.setItems(assets);
                showEmptyView(false);
                return;
            }
            binding.tvEmpty.setText(getString(R.string.info_no_assets,
                                              folder));
        } else {
            binding.tvEmpty.setText(R.string.error_fetching_assets);
        }

        showEmptyView(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(KEY_ASSETS,
                                    assets);
    }

    private void showEmptyView(boolean show) {
        if (show) {
            binding.rvAssets.setVisibility(View.GONE);
            binding.tvEmpty.setVisibility(View.VISIBLE);
            return;
        }

        binding.rvAssets.setVisibility(View.VISIBLE);
        binding.tvEmpty.setVisibility(View.GONE);
    }

    public interface OnSelectAssetListener {
        void onSelect(DialogFragment dialog, String assetName);
    }
}
