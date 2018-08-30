package sk.kamil.morley;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import sk.kamil.morley.mokka7.S7;
import sk.kamil.morley.plc.ConnectionParams;
import sk.kamil.morley.plc.DataTypeEnum;
import sk.kamil.morley.plc.RwParams;
import sk.kamil.morley.plc.ReadWriteModeEnum;
import sk.kamil.morley.services.PlcService;
import sk.kamil.morley.services.PlcServiceThread;
import timber.log.Timber;


public class MainActivity extends AppCompatActivity {

    private PlcService plcService;
    private boolean bound = false;
    private boolean connected = false;
    private EditText edtRack, edtSlot, edtIp, edtReadDbBlock, edtReadDbBlockStart, edtReadDbBlockEnd, edtEdtReadDbBlockBit, edtValue;
    private TextInputLayout tilReadDbBlockBit, tilReadDbBlock;
    private Button btnConnect, btnDisconnect, btnAction;
    private ConstraintLayout mainRoot;
    private RadioGroup rgConnectionType;
    private Spinner dbBlockType, readWriteMode;
    private TextView tvActionCardTitle;
    private Switch swReadWriteMode, swWriteBoleanValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mainRoot = findViewById(R.id.main_cl_root);
        this.btnConnect = findViewById(R.id.main_button_connect);
        this.btnDisconnect = findViewById(R.id.main_button_disconnect);
        this.btnConnect.setOnClickListener(v -> connect());
        this.btnDisconnect.setOnClickListener(v -> disconnect());
        this.btnDisconnect.setEnabled(false);
        this.edtRack = findViewById(R.id.main_edit_text_rack);
        this.edtSlot = findViewById(R.id.main_edit_text_slot);
        this.edtIp = findViewById(R.id.main_edit_text_ip);
        this.rgConnectionType = findViewById(R.id.main_rg_conn_type);
        this.swWriteBoleanValue = findViewById(R.id.main_sw_action_write_boolean_value);
        this.swReadWriteMode = findViewById(R.id.main_switch_rw_mode);
        this.swReadWriteMode.setOnCheckedChangeListener(new OnRwModeSwitchListener());
        this.btnAction = findViewById(R.id.main_button_action);
        this.btnAction.setOnClickListener(v -> doAction());
        this.edtReadDbBlock = findViewById(R.id.main_edit_text_read_db_block);
        this.edtReadDbBlockStart = findViewById(R.id.main_edit_text_read_start_position);
        this.edtReadDbBlockEnd = findViewById(R.id.main_edit_text_read_end_position);
        this.edtEdtReadDbBlockBit = findViewById(R.id.main_edit_text_db_block_boolean_bit);
        this.tilReadDbBlockBit = findViewById(R.id.main_text_input_db_block_boolean_bit);
        this.tilReadDbBlock = findViewById(R.id.main_text_imput_read_db_block);
        this.tilReadDbBlock.setEnabled(false);
        this.edtValue = findViewById(R.id.main_edit_text_value);
        this.edtValue.setOnEditorActionListener(new OnWriteValueEditorActionListener());
        this.dbBlockType = findViewById(R.id.main_spinner_read_data_type);
        this.dbBlockType.setOnItemSelectedListener(new OnDataTypeSelectListener());
        this.dbBlockType.setAdapter(new ArrayAdapter<DataTypeEnum>(this, android.R.layout.simple_list_item_1, DataTypeEnum.values()));
        this.readWriteMode = findViewById(R.id.main_spinner_read_write_mode);
        this.readWriteMode.setOnItemSelectedListener(new OnRwModeSelectListener());
        this.readWriteMode.setAdapter(new ArrayAdapter<ReadWriteModeEnum>(this, android.R.layout.simple_list_item_1, ReadWriteModeEnum.values()));
        this.tvActionCardTitle = findViewById(R.id.main_tv_action_card_title);

    }


    @Override
    protected void onStart() {
        super.onStart();

        // Register local broadcast
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(this.messageReceiver,
                new IntentFilter(PlcServiceThread.PLC_SERVICE_ACTION));

        // Bind to LocalService
        Intent intent = new Intent(this, PlcService.class);
        bindService(intent, this.serviceConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onStop() {
        super.onStop();


        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(this.messageReceiver);
        bound = false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        plcService.onDestroy();
        // Unndbind service
        unbindService(this.serviceConnection);
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlcService.PlcBinder binder = (PlcService.PlcBinder) service;
            plcService = binder.getService();
            bound = true;
            Timber.d("Bound to service ok");
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
            Timber.d("Unbound from service ok");
        }
    };


    /**
     * Connect
     */
    private void connect() {

        if (this.edtRack.getText().length() == 0
                || this.edtSlot.getText().length() == 0
                || this.edtIp.getText().length() == 0) {
            showSnackbar(R.string.all_connection_params_not_filled);
        } else {

            if (this.plcService != null) {

                this.btnConnect.setEnabled(false);
                ConnectionParams connectionParams = new ConnectionParams();
                connectionParams.setRack(Integer.parseInt(this.edtRack.getText().toString()));
                connectionParams.setSlot(Integer.parseInt(this.edtSlot.getText().toString()));
                connectionParams.setConnectionType(getConnectionType());
                connectionParams.setIpAddress(this.edtIp.getText().toString());
                this.plcService.connectToPlc(connectionParams);
            }
        }

    }

    /**
     * Disconnect
     */
    private void disconnect() {
        if (this.plcService != null) {
            this.plcService.disconnectFromPlc();
            this.btnConnect.setEnabled(true);
            this.btnDisconnect.setEnabled(false);
            this.connected = false;
        }
    }

    /**
     * Write / Read action
     */
    private void doAction() {
        if (this.connected && this.plcService != null) {


            RwParams rwParams = new RwParams();
            rwParams.setDbBolockNumber(Integer.parseInt(edtReadDbBlock.getText().toString()));
            rwParams.setStart(Integer.parseInt(edtReadDbBlockStart.getText().toString()));
            rwParams.setEnd(Integer.parseInt(edtReadDbBlockEnd.getText().toString()));
            rwParams.setBit(Integer.parseInt(edtEdtReadDbBlockBit.getText().toString()));
            DataTypeEnum dataType = (DataTypeEnum) dbBlockType.getSelectedItem();
            rwParams.setDataType(dataType);
            ReadWriteModeEnum rwMode = (ReadWriteModeEnum) readWriteMode.getSelectedItem();
            rwParams.setReadWriteMode(rwMode);

            if (this.swReadWriteMode.isChecked()) {

                if (dataType != DataTypeEnum.BOOLEAN && edtValue.getText().length() == 0) {
                    showSnackbar(R.string.all_no_value);
                    return;
                }

                switch (dataType) {

                    case BOOLEAN:
                        rwParams.setWriteData(swWriteBoleanValue.isChecked());
                        break;
                    case WORD:
                        rwParams.setWriteData(Integer.parseInt(edtValue.getText().toString()));
                        break;
                    case REAL:
                        rwParams.setWriteData(Float.parseFloat(edtValue.getText().toString()));
                        break;
                    default:
                        rwParams.setWriteData(edtValue.getText().toString());
                }

                this.plcService.write(rwParams);

            } else {

                this.plcService.read(rwParams);
            }

        } else {
            showSnackbar(R.string.all_no_connection);
        }

    }


    /**
     * DataType select listener
     */
    private class OnDataTypeSelectListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            // Boolean
            DataTypeEnum dataType = (DataTypeEnum) parent.getItemAtPosition(position);
            if (dataType == DataTypeEnum.BOOLEAN) {
                tilReadDbBlockBit.setVisibility(View.VISIBLE);
                updateValueInputUi(swReadWriteMode.isChecked(), dataType);

            } else { // other
                tilReadDbBlockBit.setVisibility(View.GONE);
                updateValueInputUi(swReadWriteMode.isChecked(), dataType);
            }


        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    /**
     * Read / Write mode select listener
     */
    private class OnRwModeSelectListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            // DB block
            if (position == 3) {
                tilReadDbBlock.setEnabled(true);
            } else {
                tilReadDbBlock.setEnabled(false);
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    /**
     * Read / Write mode switch listener
     */
    private class OnRwModeSwitchListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            DataTypeEnum dataType = (DataTypeEnum) dbBlockType.getSelectedItem();

            if (isChecked) {
                setWriteMode(dataType);
            } else {
                setReadMode(dataType);
            }

        }
    }

    /**
     * Write value editor action listener
     */
    private class OnWriteValueEditorActionListener implements TextView.OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                doAction();
                hideKeyboard();
                return true;
            }
            return false;
        }

    }


    /**
     * Receiver for callback from service
     */
    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int actionResult = intent.getIntExtra(PlcServiceThread.PLC_SERVICE_CALLBACK, -1);
            String actionResultErrorMessage = intent.getStringExtra(PlcServiceThread.PLC_SERVICE_CALLBACK_ERROR);

            switch (actionResult) {

                case PlcServiceThread.CONNECT_SUCCESS:
                    showSnackbar(R.string.all_connection_successful);
                    btnConnect.setEnabled(false);
                    btnDisconnect.setEnabled(true);
                    connected = true;
                    break;

                case PlcServiceThread.CONNECT_FAULT:
                    showSnackbar(R.string.all_connection_unsuccessful, actionResultErrorMessage);
                    btnConnect.setEnabled(true);
                    btnDisconnect.setEnabled(false);
                    connected = false;
                    break;

                case PlcServiceThread.READ_SUCCESS:
                    updateReadValue(intent.getStringExtra(PlcServiceThread.READ_VALUE));
                    break;

                case PlcServiceThread.READ_FAULT:
                    updateReadValue(null);
                    showSnackbar(R.string.all_read_failed, actionResultErrorMessage);
                    break;

                case PlcServiceThread.WRITE_FAULT:
                    updateReadValue(null);
                    showSnackbar(R.string.all_write_failed, actionResultErrorMessage);
                    break;

            }

        }
    };

    /**
     * Helper for getting connection type
     *
     * @return
     */
    private byte getConnectionType() {

        int selectedId = this.rgConnectionType.getCheckedRadioButtonId();

        switch (selectedId) {

            case R.id.main_rb_op:
                return S7.OP;
            case R.id.main_rb_pg:
                return S7.PG;
            default:
                return S7.S7_BASIC;

        }

    }

    /**
     * Method for set write mode in UI
     *
     * @param dataTypeEnum
     */
    private void setWriteMode(DataTypeEnum dataTypeEnum) {

        this.tvActionCardTitle.setText(R.string.all_write);
        this.btnAction.setText(R.string.all_write);
        this.edtValue.getText().clear();
        updateValueInputUi(true, dataTypeEnum);


    }

    /**
     * Method for set read mode in UI
     *
     * @param dataTypeEnum
     */
    private void setReadMode(DataTypeEnum dataTypeEnum) {
        this.tvActionCardTitle.setText(R.string.all_read);
        this.btnAction.setText(R.string.all_read);
        this.edtValue.setText(R.string.all_value_empty);
        updateValueInputUi(false, dataTypeEnum);
    }


    /**
     * Method for update read value
     *
     * @param value
     */
    private void updateReadValue(String value) {
        if (value != null) {
            this.edtValue.setText(value);
        } else {
            this.edtValue.setText(R.string.all_value_empty);
        }
    }

    /**
     * Method for update value input UI
     *
     * @param isWriteMode
     * @param dataType
     */
    private void updateValueInputUi(boolean isWriteMode, DataTypeEnum dataType) {


        this.edtValue.setEnabled(isWriteMode);

        switch (dataType) {

            case BOOLEAN:

                this.btnAction.setEnabled(true);
                this.edtValue.getText().clear();
                if (isWriteMode) {
                    this.swWriteBoleanValue.setVisibility(View.VISIBLE);
                    this.edtValue.setVisibility(View.INVISIBLE);
                } else {
                    this.swWriteBoleanValue.setVisibility(View.GONE);
                    this.edtValue.setVisibility(View.VISIBLE);
                }
                break;
            case WORD:
                this.btnAction.setEnabled(true);
                this.edtValue.getText().clear();
                edtValue.setInputType(InputType.TYPE_CLASS_NUMBER);
                this.swWriteBoleanValue.setVisibility(View.GONE);
                this.edtValue.setVisibility(View.VISIBLE);
                break;
            case REAL:
                this.btnAction.setEnabled(true);
                this.edtValue.getText().clear();
                edtValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                this.swWriteBoleanValue.setVisibility(View.GONE);
                this.edtValue.setVisibility(View.VISIBLE);
                break;
            default: // String

                edtValue.setInputType(InputType.TYPE_CLASS_TEXT);
                this.swWriteBoleanValue.setVisibility(View.GONE);
                this.edtValue.setVisibility(View.VISIBLE);
                if (isWriteMode) {
                    this.edtValue.setText(R.string.all_value_no_available);
                    this.btnAction.setEnabled(false);
                    this.edtValue.setEnabled(false);
                } else {
                    this.edtValue.setText(R.string.all_value_empty);
                    this.btnAction.setEnabled(true);
                }

        }

    }

    private void showSnackbar(int messageResource, String error) {
        String msg = String.format(getResources().getString(messageResource), error);
        showSnackbar(msg, Snackbar.LENGTH_LONG);
    }

    private void showSnackbar(int messageResource) {
        String msg = getResources().getString(messageResource);
        showSnackbar(msg, Snackbar.LENGTH_SHORT);
    }


    private void showSnackbar(String msg, int length) {

        // Hide keyboard always
        hideKeyboard();
        Snackbar snackbar = Snackbar
                .make(this.mainRoot, msg, length);
        snackbar.show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(this.mainRoot.getWindowToken(), 0);
        }

    }


}
