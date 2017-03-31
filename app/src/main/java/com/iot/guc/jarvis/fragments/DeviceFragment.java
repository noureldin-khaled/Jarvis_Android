package com.iot.guc.jarvis.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iot.guc.jarvis.Constants;
import com.iot.guc.jarvis.Popup;
import com.iot.guc.jarvis.R;
import com.iot.guc.jarvis.Shared;
import com.iot.guc.jarvis.adapters.DeviceAdapter;
import com.iot.guc.jarvis.adapters.ScannedDeviceAdapter;
import com.iot.guc.jarvis.models.Device;
import com.iot.guc.jarvis.models.Room;
import com.iot.guc.jarvis.responses.HTTPResponse;
import com.iot.guc.jarvis.responses.PopupResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DeviceFragment extends Fragment {
    private CoordinatorLayout DeviceFragment_CoordinatorLayout_MainContentView;
    private ListView DeviceFragment_ListView_Devices;
    private TextView DeviceFragment_TextView_NoDevicesFound;
    private DeviceAdapter adapter;
    private Room room;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);
        room = Shared.getRooms().get(Shared.getSelectedRoom());
        DeviceFragment_CoordinatorLayout_MainContentView = (CoordinatorLayout) view.findViewById(R.id.DeviceFragment_CoordinatorLayout_MainContentView);
        DeviceFragment_TextView_NoDevicesFound = (TextView) view.findViewById(R.id.DeviceFragment_TextView_NoDevicesFound);

        FloatingActionButton DeviceFragment_FloatingActionButton_AddDevice = (FloatingActionButton) view.findViewById(R.id.DeviceFragment_FloatingActionButton_AddDevice);
        DeviceFragment_FloatingActionButton_AddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanDevices();
            }
        });

        ImageButton DeviceFragment_ImageButton_BackButton = (ImageButton) view.findViewById(R.id.DeviceFragment_ImageButton_BackButton);
        DeviceFragment_ImageButton_BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Shared.setSelectedRoom(-1);

                FragmentTransaction trans = getFragmentManager().beginTransaction();
                trans.replace(R.id.ContainerFragment_FrameLayout_Container, new RoomFragment());

                trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                trans.addToBackStack(null);

                trans.commit();
            }
        });

        if (Shared.getAuth().getType().equalsIgnoreCase("Normal"))
            DeviceFragment_FloatingActionButton_AddDevice.setVisibility(View.GONE);

        TextView DeviceFragment_TextView_RoomName = (TextView) view.findViewById(R.id.DeviceFragment_TextView_RoomName);
        DeviceFragment_TextView_RoomName.setText(room.getName());

        DeviceFragment_ListView_Devices = (ListView) view.findViewById(R.id.DeviceFragment_ListView_Devices);
        DeviceFragment_ListView_Devices.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(250);

                LayoutInflater inflater = getActivity().getLayoutInflater();
                View contentView = inflater.inflate(R.layout.dialog_options, null);
                final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(contentView).create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.show();
                dialog.getWindow().setLayout(Shared.dpToPx(240, getContext()), ViewGroup.LayoutParams.WRAP_CONTENT);

                final FloatingActionButton OptionDialog_FloatingActionButton_Edit = (FloatingActionButton) contentView.findViewById(R.id.OptionDialog_FloatingActionButton_Edit);
                final FloatingActionButton OptionDialog_FloatingActionButton_Delete = (FloatingActionButton) contentView.findViewById(R.id.OptionDialog_FloatingActionButton_Delete);

                OptionDialog_FloatingActionButton_Edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        editDevice(position);
                    }
                });

                OptionDialog_FloatingActionButton_Delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        deleteDevice(position);
                    }
                });

                return true;
            }
        });

        reload();
        return view;
    }

    public void reload() {
        ArrayList<Device> a = Shared.getDevices(room.getId());
        adapter = new DeviceAdapter(getContext(), a);
        DeviceFragment_ListView_Devices.setAdapter(adapter);
        if (a.isEmpty())
            DeviceFragment_TextView_NoDevicesFound.setVisibility(View.VISIBLE);
        else
            DeviceFragment_TextView_NoDevicesFound.setVisibility(View.GONE);
    }

    public void scanDevices() {
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View contentView = inflater.inflate(R.layout.dialog_scan_devices, null);
        final LinearLayout ScanDeviceDialog_LinearLayout_Progress = (LinearLayout) contentView.findViewById(R.id.ScanDeviceDialog_LinearLayout_Progress);
        final LinearLayout ScanDeviceDialog_LinearLayout_DevicesLayout = (LinearLayout) contentView.findViewById(R.id.ScanDeviceDialog_LinearLayout_DevicesLayout);
        final ListView ScanDeviceDialog_ListView_DeviceList = (ListView) contentView.findViewById(R.id.ScanDeviceDialog_ListView_DeviceList);
        final TextView ScanDeviceDialog_TextView_Title = (TextView) contentView.findViewById(R.id.ScanDeviceDialog_TextView_Title);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setView(contentView)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();

        dialog.show();

        ScanDeviceDialog_LinearLayout_Progress.setVisibility(View.VISIBLE);
        ScanDeviceDialog_LinearLayout_DevicesLayout.setVisibility(View.GONE);

        Device.scanDevices(getContext(), new HTTPResponse() {
            @Override
            public void onSuccess(int statusCode, JSONObject body) {
                ScanDeviceDialog_LinearLayout_Progress.setVisibility(View.GONE);
                ScanDeviceDialog_LinearLayout_DevicesLayout.setVisibility(View.VISIBLE);

                try {
                    JSONArray devicesJson = body.getJSONArray("devices");
                    ArrayList<Device> devices = new ArrayList<>();
                    for (int i = 0; i < devicesJson.length(); i++) {
                        JSONObject current = devicesJson.getJSONObject(i);
                        devices.add(new Device(current.getString("type").equals("Light Bulb") ? Device.TYPE.LIGHT_BULB : Device.TYPE.LOCK, current.getString("mac"), current.getString("ip")));
                    }

                    final ScannedDeviceAdapter adapter = new ScannedDeviceAdapter(getActivity(), devices);
                    ScanDeviceDialog_TextView_Title.setText(devices.isEmpty() ? "No Devices Detected" : "Devices Found");
                    ScanDeviceDialog_ListView_DeviceList.setAdapter(adapter);

                    ScanDeviceDialog_ListView_DeviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            final Device device = (Device) adapter.getItem(position);
                            dialog.dismiss();
                            addDevice(device);
                        }
                    });
                } catch (JSONException e) {
                    dialog.dismiss();
                    Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    scanDevices();
                                }
                            }).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, JSONObject body) {
                dialog.dismiss();
                switch (statusCode) {
                    case Constants.NO_INTERNET_CONNECTION: {
                        Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                    }
                    break;
                    case Constants.SERVER_NOT_REACHED: {
                        Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        scanDevices();
                                    }
                                }).show();
                    }
                    break;
                    default: {
                        Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        scanDevices();
                                    }
                                }).show();
                    }
                }
            }
        });
    }

    public void addDevice(final Device device) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_add_device, null);

        final TextView AddDeviceDialog_TextView_DeviceMac = (TextView) contentView.findViewById(R.id.AddDeviceDialog_TextView_DeviceMac);
        AddDeviceDialog_TextView_DeviceMac.setText(device.getMac().toUpperCase());

        final TextInputLayout AddDeviceDialog_TextInputLayout_DeviceNameLayout = (TextInputLayout) contentView.findViewById(R.id.AddDeviceDialog_TextInputLayout_DeviceNameLayout);
        final EditText AddDeviceDialog_EditText_DeviceName = (EditText) contentView.findViewById(R.id.AddDeviceDialog_EditText_DeviceName);
        final ProgressBar AddDeviceDialog_ProgressBar_Progress = (ProgressBar) contentView.findViewById(R.id.AddDeviceDialog_ProgressBar_Progress);
        final LinearLayout AddDeviceDialog_LinearLayout_AddDeviceForm = (LinearLayout) contentView.findViewById(R.id.AddDeviceDialog_LinearLayout_AddDeviceForm);

        new Popup().create(getActivity(), contentView, "Add", new PopupResponse() {

            @Override
            public void onPositive(final AlertDialog dialog) {
                AddDeviceDialog_TextInputLayout_DeviceNameLayout.setErrorEnabled(false);
                AddDeviceDialog_TextInputLayout_DeviceNameLayout.setError(null);

                if (AddDeviceDialog_EditText_DeviceName.getText().toString().isEmpty()) {
                    AddDeviceDialog_TextInputLayout_DeviceNameLayout.setErrorEnabled(true);
                    AddDeviceDialog_TextInputLayout_DeviceNameLayout.setError("Please Enter a Device Name");
                }

                if (!AddDeviceDialog_EditText_DeviceName.getText().toString().isEmpty()) {
                    AddDeviceDialog_ProgressBar_Progress.setVisibility(View.VISIBLE);
                    AddDeviceDialog_LinearLayout_AddDeviceForm.setVisibility(View.GONE);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);

                    Device.addDevice(getContext(), AddDeviceDialog_EditText_DeviceName.getText().toString(),
                            device.getType(), device.getMac(), device.getIp(), room.getId(),
                            new HTTPResponse() {
                                @Override
                                public void onSuccess(int statusCode, JSONObject body) {
                                    Shared.collapseKeyBoard(DeviceFragment.this);
                                    dialog.dismiss();

                                    try {
                                        JSONObject jsonDevice = body.getJSONObject("device");
                                        Shared.addDevice(new Device(jsonDevice.getInt("id") , jsonDevice.getString("name"),
                                                jsonDevice.getString("type").equals("Light Bulb") ? Device.TYPE.LIGHT_BULB : Device.TYPE.LOCK, jsonDevice.getBoolean("status"),
                                                jsonDevice.getString("mac"), jsonDevice.getString("ip"), jsonDevice.getInt("room_id")));
                                        reload();
                                        Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Device Created Successfully", Snackbar.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                .setAction("RETRY", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        scanDevices();
                                                    }
                                                }).show();
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(int statusCode, JSONObject body) {
                                    switch (statusCode) {
                                        case Constants.NO_INTERNET_CONNECTION: {
                                            Shared.collapseKeyBoard(DeviceFragment.this);
                                            dialog.dismiss();
                                            Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                        }
                                        break;
                                        case Constants.SERVER_NOT_REACHED: {
                                            Shared.collapseKeyBoard(DeviceFragment.this);
                                            dialog.dismiss();
                                            Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG)
                                                    .setAction("RETRY", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            scanDevices();
                                                        }
                                                    }).show();
                                        }
                                        break;
                                        case 400: {
                                            try {
                                                JSONArray arr = body.getJSONArray("errors");
                                                for (int i = 0; i < arr.length(); i++) {
                                                    JSONObject current = arr.getJSONObject(i);
                                                    String type = current.getString("msg");
                                                    String field = current.getString("param");

                                                    if (type.equals("required")) {
                                                        if (field.equals("name")) {
                                                            AddDeviceDialog_TextInputLayout_DeviceNameLayout.setErrorEnabled(true);
                                                            AddDeviceDialog_TextInputLayout_DeviceNameLayout.setError("Please Enter a Device Name");
                                                        }
                                                        else {
                                                            Shared.collapseKeyBoard(DeviceFragment.this);
                                                            dialog.dismiss();
                                                            Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                                    .setAction("RETRY", new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            scanDevices();
                                                                        }
                                                                    }).show();
                                                            break;
                                                        }
                                                    }
                                                    else if (type.equals("unique violation")) {
                                                        if (field.equals("name")) {
                                                            AddDeviceDialog_TextInputLayout_DeviceNameLayout.setErrorEnabled(true);
                                                            AddDeviceDialog_TextInputLayout_DeviceNameLayout.setError("This name is already taken.");
                                                        }
                                                        else {
                                                            Shared.collapseKeyBoard(DeviceFragment.this);
                                                            dialog.dismiss();
                                                            Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                                    .setAction("RETRY", new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            scanDevices();
                                                                        }
                                                                    }).show();
                                                            break;
                                                        }
                                                    }
                                                    else {
                                                        Shared.collapseKeyBoard(DeviceFragment.this);
                                                        dialog.dismiss();
                                                        Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                                .setAction("RETRY", new View.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(View v) {
                                                                        scanDevices();
                                                                    }
                                                                }).show();
                                                        break;
                                                    }
                                                }
                                            } catch (JSONException e) {
                                                Shared.collapseKeyBoard(DeviceFragment.this);
                                                dialog.dismiss();
                                                Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                        .setAction("RETRY", new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                scanDevices();
                                                            }
                                                        }).show();
                                                e.printStackTrace();
                                            }
                                        }
                                        break;
                                        default: {
                                            Shared.collapseKeyBoard(DeviceFragment.this);
                                            dialog.dismiss();
                                            Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                    .setAction("RETRY", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            scanDevices();
                                                        }
                                                    }).show();
                                        }
                                    }
                                }
                            });
                }
            }

            @Override
            public void onNegative(AlertDialog dialog) {
                Shared.collapseKeyBoard(DeviceFragment.this);
                dialog.dismiss();
            }
        });
    }

    public void editDevice(final int position) {
        final Device device = (Device) adapter.getItem(position);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_add_room, null);

        TextView AddRoomDialog_TextView_Title =(TextView) contentView.findViewById(R.id.AddRoomDialog_TextView_Title);
        AddRoomDialog_TextView_Title.setText("Edit Device");
        final TextInputLayout AddRoomDialog_TextInputLayout_RoomNameLayout = (TextInputLayout) contentView.findViewById(R.id.AddRoomDialog_TextInputLayout_RoomNameLayout);
        final EditText AddRoomDialog_EditText_RoomName = (EditText) contentView.findViewById(R.id.AddRoomDialog_EditText_RoomName);
        final ProgressBar AddRoomDialog_ProgressBar_Progress = (ProgressBar) contentView.findViewById(R.id.AddRoomDialog_ProgressBar_Progress);
        final LinearLayout AddRoomDialog_LinearLayout_AddRoomForm = (LinearLayout) contentView.findViewById(R.id.AddRoomDialog_LinearLayout_AddRoomForm);

        new Popup().create(getActivity(), contentView, "Save", new PopupResponse() {
            @Override
            public void onPositive(final AlertDialog dialog) {
                AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(false);
                AddRoomDialog_TextInputLayout_RoomNameLayout.setError(null);

                if (AddRoomDialog_EditText_RoomName.getText().toString().isEmpty()) {
                    AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(true);
                    AddRoomDialog_TextInputLayout_RoomNameLayout.setError("Please Enter a Device Name");
                }

                if (!AddRoomDialog_EditText_RoomName.getText().toString().isEmpty()) {
                    AddRoomDialog_ProgressBar_Progress.setVisibility(View.VISIBLE);
                    AddRoomDialog_LinearLayout_AddRoomForm.setVisibility(View.GONE);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);

                    final String new_name = AddRoomDialog_EditText_RoomName.getText().toString();

                    device.editDevice(getContext(), new_name, new HTTPResponse() {
                        @Override
                        public void onSuccess(int statusCode, JSONObject body) {
                            Shared.collapseKeyBoard(DeviceFragment.this);
                            dialog.dismiss();
                            int idx = -1;
                            for(int i = 0; i < Shared.getDevices().size() && idx == -1; i++){
                                if(device.getId() == Shared.getDevices().get(i).getId())
                                    idx = i;
                            }

                            Shared.editDevice(idx, new Device(device.getId(), new_name, device.getType(), device.isStatus(), device.getMac(), device.getIp(), device.getRoom_id()));
                            reload();
                            Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Device Saved Successfully", Snackbar.LENGTH_LONG).show();
                        }

                        @Override
                        public void onFailure(int statusCode, JSONObject body) {
                            AddRoomDialog_ProgressBar_Progress.setVisibility(View.GONE);
                            AddRoomDialog_LinearLayout_AddRoomForm.setVisibility(View.VISIBLE);

                            switch (statusCode) {
                                case Constants.NO_INTERNET_CONNECTION: {
                                    Shared.collapseKeyBoard(DeviceFragment.this);
                                    dialog.dismiss();
                                    Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                }
                                break;
                                case Constants.SERVER_NOT_REACHED: {
                                    Shared.collapseKeyBoard(DeviceFragment.this);
                                    dialog.dismiss();
                                    Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG)
                                            .setAction("RETRY", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    editDevice(position);
                                                }
                                            }).show();
                                }
                                break;
                                case 400: {
                                    try {
                                        JSONArray arr = body.getJSONArray("errors");
                                        for (int i = 0; i < arr.length(); i++) {
                                            JSONObject current = arr.getJSONObject(i);
                                            String type = current.getString("msg");
                                            String field = current.getString("param");

                                            if (type.equals("unique violation")) {
                                                if (field.equals("name")) {
                                                    AddRoomDialog_TextInputLayout_RoomNameLayout.setErrorEnabled(true);
                                                    AddRoomDialog_TextInputLayout_RoomNameLayout.setError("This name is already taken.");
                                                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                                                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
                                                }
                                                else {
                                                    Shared.collapseKeyBoard(DeviceFragment.this);
                                                    dialog.dismiss();
                                                    Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                            .setAction("RETRY", new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    editDevice(position);
                                                                }
                                                            }).show();
                                                    break;
                                                }
                                            }
                                            else {
                                                Shared.collapseKeyBoard(DeviceFragment.this);
                                                dialog.dismiss();
                                                Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                        .setAction("RETRY", new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                editDevice(position);
                                                            }
                                                        }).show();
                                                break;
                                            }
                                        }

                                    } catch (JSONException e) {
                                        Shared.collapseKeyBoard(DeviceFragment.this);
                                        dialog.dismiss();
                                        Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                .setAction("RETRY", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        editDevice(position);
                                                    }
                                                }).show();
                                        e.printStackTrace();
                                    }

                                }
                                break;
                                default: {
                                    Shared.collapseKeyBoard(DeviceFragment.this);
                                    dialog.dismiss();
                                    Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                            .setAction("RETRY", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    editDevice(position);
                                                }
                                            }).show();
                                }
                            }
                        }
                    });
                }


            }

            @Override
            public void onNegative(AlertDialog dialog) {
                Shared.collapseKeyBoard(DeviceFragment.this);
                dialog.dismiss();
            }
        });
    }

    public void deleteDevice(final int position){
        final Device device = (Device) adapter.getItem(position);
        new AlertDialog.Builder(getActivity()).setTitle("Confirmation")
                .setMessage("Are you sure you want to delete this Device?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View contentView = inflater.inflate(R.layout.dialog_loading, null);
                        final AlertDialog loading = new AlertDialog.Builder(getContext()).setView(contentView).create();
                        loading.show();

                        device.deleteDevice(getContext(), new HTTPResponse() {
                            @Override
                            public void onSuccess(int statusCode, JSONObject body) {
                                Shared.removeDevice(device);
                                reload();
                                loading.dismiss();
                                Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Device Deleted Successfully", Snackbar.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(int statusCode, JSONObject body) {
                                loading.dismiss();
                                switch (statusCode) {
                                    case Constants.NO_INTERNET_CONNECTION: {
                                        Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "No Internet Connection!", Snackbar.LENGTH_LONG).show();
                                    }
                                    break;
                                    case Constants.SERVER_NOT_REACHED: {
                                        Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Server Can\'t Be Reached!", Snackbar.LENGTH_LONG)
                                                .setAction("RETRY", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        deleteDevice(position);
                                                    }
                                                }).show();
                                    }
                                    break;
                                    default: {
                                        Snackbar.make(DeviceFragment_CoordinatorLayout_MainContentView, "Something Went Wrong!", Snackbar.LENGTH_LONG)
                                                .setAction("RETRY", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        deleteDevice(position);
                                                    }
                                                }).show();
                                    }
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
    }
}
