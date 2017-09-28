package com.imkorn.listatsdtest.ui.fragment;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.imkorn.listatsdtest.R;
import com.imkorn.listatsdtest.databinding.FragmentDisplayBinding;
import com.imkorn.listatsdtest.model.ComposeDisplay;
import com.imkorn.listatsdtest.model.Aggregator;
import com.imkorn.listatsdtest.model.SchedulerDisplay;
import com.imkorn.listatsdtest.model.Socket;
import com.imkorn.listatsdtest.model.entities.PrimeNumber;
import com.imkorn.listatsdtest.model.PrimeNumberSearchHelper;
import com.imkorn.listatsdtest.parser.exceptions.ParseException;
import com.imkorn.listatsdtest.ui.adapters.PrimeNumbersAdapter;
import com.imkorn.listatsdtest.ui.dialogs.SelectAssetDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

/**
 * Created by imkorn on 22.09.17.
 */

public class MainFragment extends Fragment implements SelectAssetDialog.OnSelectAssetListener {
    // Constants
    private static final String DIALOG_TAG_SELECT_ASSET = "DIALOG_TAG_SELECT_ASSET";

    // Data
    private PrimeNumberSearchHelper primeNumberSearchHelper;

    // Ui
    private FragmentDisplayBinding binding;

    // Adapters
    private PrimeNumbersAdapter primeNumbersAdapter;

    // Persistence
    private Collection<PrimeNumber> primeNumbers;
    private SchedulerDisplay display;
    private LinearLayoutManager llm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        display = new SchedulerDisplay(Looper.getMainLooper());

        Socket socket = new Socket(display);

        primeNumberSearchHelper = new PrimeNumberSearchHelper("PrimeNumberSearchHelper:0",
                                                              display,
                                                              new Aggregator(socket, display));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_display,
                                container,
                                false);
    }

    @Override
    public void onViewCreated(View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,
                            savedInstanceState);
        binding = FragmentDisplayBinding.bind(view);

        final Activity activity = getActivity();

        primeNumbersAdapter = new PrimeNumbersAdapter(activity);
        primeNumbersAdapter.setItems(primeNumbers);

        binding.rvPrimeNumbers.setAdapter(primeNumbersAdapter);
        binding.rvPrimeNumbers.addItemDecoration(new RecyclerView.ItemDecoration() {
            private int gap = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                              4,
                                                              getResources().getDisplayMetrics());

            @Override
            public void getItemOffsets(Rect outRect,
                                       View view,
                                       RecyclerView parent,
                                       RecyclerView.State state) {
                if (parent.getChildAdapterPosition(view) == 0) {
                    outRect.top = gap;
                }
                outRect.bottom = gap;
            }
        });
        llm = new LinearLayoutManager(activity);
        binding.rvPrimeNumbers.setLayoutManager(llm);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        primeNumbers = primeNumbersAdapter.getAll();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,
                                  inflater);

        inflater.inflate(R.menu.fragment_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_select_asset: {
                SelectAssetDialog.newInstance(PrimeNumberSearchHelper.SRC_FOLDER)
                                 .show(getChildFragmentManager(),
                                       DIALOG_TAG_SELECT_ASSET);
                return true;
            }
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSelect(DialogFragment dialog,
                         String assetName) {
        dialog.dismiss();

        final StringBuilder content = new StringBuilder();

        primeNumbersAdapter.clear();

        try {
            final String link = PrimeNumberSearchHelper.SRC_FOLDER + '/' + assetName;

            final BufferedReader stream =
                    new BufferedReader(new InputStreamReader(getResources().getAssets()
                                                                           .open(link)));
            String line;
            while ((line = stream.readLine()) != null) {
                content.append(line);
            }
            stream.close();

            primeNumberSearchHelper.parseAndFind(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),
                           "Error occurred while reading asset",
                           Toast.LENGTH_SHORT)
                 .show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        display.setDisplay(new ComposeDisplay() {
            @Override
            public void displayResult(@NonNull Collection<PrimeNumber> primeNumbers) {
                primeNumbersAdapter.addItems(primeNumbers);
                Log.i("TAGG",
                      "displayResult: " + primeNumbers.size());
                llm.smoothScrollToPosition(binding.rvPrimeNumbers, null, primeNumbersAdapter.getItemCount() - 1);
            }

            @Override
            public void displayError(@NonNull Throwable throwable) {
                final String msg;
                if (throwable instanceof ParseException) {
                    msg = "Invalid asset";
                } else {
                    msg = throwable.getMessage();
                }

                Toast.makeText(getActivity(),
                               msg,
                               Toast.LENGTH_SHORT)
                     .show();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        display.setDisplay(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        primeNumberSearchHelper.close();
    }
}
