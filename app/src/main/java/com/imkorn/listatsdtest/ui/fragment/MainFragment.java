package com.imkorn.listatsdtest.ui.fragment;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.imkorn.listatsdtest.model.Display;
import com.imkorn.listatsdtest.model.SaveDisplay;
import com.imkorn.listatsdtest.model.TcpSocket;
import com.imkorn.listatsdtest.model.entities.PrimeNumber;
import com.imkorn.listatsdtest.model.PrimeNumberSearchHelper;
import com.imkorn.listatsdtest.parser.exceptions.ParseException;
import com.imkorn.listatsdtest.ui.adapters.PrimeNumbersAdapter;
import com.imkorn.listatsdtest.ui.dialogs.SelectAssetDialog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;

import static com.imkorn.listatsdtest.model.SaveDisplay.State.STATE_DISCONNECTED;
import static com.imkorn.listatsdtest.model.SaveDisplay.State.STATE_RECONNECTING;
import static com.imkorn.listatsdtest.model.SaveDisplay.State.STATE_SENDING_DATA;
import static com.imkorn.listatsdtest.model.SaveDisplay.State.STATE_SENT;
import static com.imkorn.listatsdtest.model.SaveDisplay.State.STATE_TERMINATED;
import static com.imkorn.listatsdtest.model.SaveDisplay.State.STATE_WAITING_CONNECTION;
import static com.imkorn.listatsdtest.model.SaveDisplay.State.STATE_WAITING_DATA;

/**
 * Created by imkorn on 22.09.17.
 */

public class MainFragment extends Fragment implements SelectAssetDialog.OnSelectAssetListener {
    // Constants
    private static final String DIALOG_TAG_SELECT_ASSET = "DIALOG_TAG_SELECT_ASSET";

    // Data
    private PrimeNumberSearchHelper primeNumberSearchHelper;
    private SaveDisplay displayer;

    // Ui
    private FragmentDisplayBinding binding;

    // Adapters
    private PrimeNumbersAdapter primeNumbersAdapter;

    // Persistence
    private Collection<PrimeNumber> primeNumbers;
    private TcpSocket socket;
    private MenuItem connectMenuItem;
    private MenuItem disconnectMenuItem;
    private MenuItem receiveMenuItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        displayer = new SaveDisplay(Looper.getMainLooper());
        primeNumberSearchHelper = new PrimeNumberSearchHelper("PrimeNumberSearchHelper:0",
                                                              displayer);
        socket = displayer.getTcpSocket();
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
        binding.rvPrimeNumbers.setLayoutManager(new LinearLayoutManager(activity));
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

        connectMenuItem = menu.findItem(R.id.mi_connect);
        disconnectMenuItem = menu.findItem(R.id.mi_disconnect);
        receiveMenuItem = menu.findItem(R.id.mi_receive_data);

        if (displayer.getSendingState() == STATE_WAITING_DATA) {
            connectMenuItem.setVisible(false);
            disconnectMenuItem.setVisible(false);
            receiveMenuItem.setVisible(false);
            return;
        }

        if (socket.isConnected()) {
            connectMenuItem.setVisible(false);
            disconnectMenuItem.setVisible(true);
            receiveMenuItem.setVisible(true);
        } else {
            connectMenuItem.setVisible(true);
            disconnectMenuItem.setVisible(false);
            receiveMenuItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_connect: {
                item.setVisible(false);
                disconnectMenuItem.setVisible(true);
                receiveMenuItem.setVisible(true);
                socket.connect();
                return true;
            }
            case R.id.mi_disconnect: {
                item.setVisible(false);
                receiveMenuItem.setVisible(false);
                connectMenuItem.setVisible(true);
                socket.disconnect();
                return true;
            }
            case R.id.mi_receive_data: {
                item.setVisible(false);
                disconnectMenuItem.setVisible(false);
                socket.receive();
                return true;
            }
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
        displayer.setDisplay(new Display() {
            @Override
            public void display(@NonNull Collection<PrimeNumber> primeNumbers) {
                primeNumbersAdapter.setItems(primeNumbers);
                binding.rvPrimeNumbers.getLayoutManager().scrollToPosition(0);
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

        final SaveDisplay.EventListener eventListener = new SaveDisplay.EventListener() {
            @Override
            public void onStateChange(@SaveDisplay.State int state) {
                switch (state) {
                    case STATE_WAITING_DATA: {
                        getActivity().setTitle("Waiting data...");
                        break;
                    }
                    case STATE_DISCONNECTED: {
                        getActivity().setTitle("Disconnected");
                        break;
                    }
                    case STATE_RECONNECTING: {
                        getActivity().setTitle("Reconnecting...");
                        break;
                    }
                    case STATE_SENDING_DATA: {
                        getActivity().setTitle("Sending data...");
                        break;
                    }
                    case STATE_SENT: {
                        Toast.makeText(getActivity(),
                                       "Sent",
                                       Toast.LENGTH_SHORT)
                             .show();
                        break;
                    }
                    case STATE_TERMINATED: {
                        getActivity().setTitle("Terminated");
                        break;
                    }
                    case STATE_WAITING_CONNECTION: {
                        if (connectMenuItem != null) {
                            connectMenuItem.setVisible(true);
                        }
                        getActivity().setTitle("Waiting connection");
                        break;
                    }
                }
            }
        };
        displayer.setEventListener(eventListener);
        eventListener.onStateChange(displayer.getSendingState());
    }

    @Override
    public void onStop() {
        super.onStop();
        displayer.setEventListener(null);
        displayer.setDisplay(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        displayer.close();
        primeNumberSearchHelper.close();
    }
}
