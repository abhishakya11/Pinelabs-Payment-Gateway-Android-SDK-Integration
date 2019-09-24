package com.app.test;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;


import com.app.test.interfaces.PinePGService;
import com.pinelabs.sdk.HashingAlgorithm;
import com.pinelabs.sdk.IPinePGResponseCallback;
import com.pinelabs.sdk.Order;
import com.pinelabs.sdk.PinePGConfig;
import com.pinelabs.sdk.PinePGPaymentManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    EditText Amount;
    EditText MerchantID;
    EditText ProductCode;
    EditText UniqueMarchantID;
    EditText MerchantAuthCode;
    EditText ThemeID;
    EditText SecureSecretKey;
    RadioGroup Theme,Production_Request,Action_Bar;
    RadioButton themeaction,productionRequestAction,ActionBarAction;
    Button Post;
    String amount,Merchantid,Productcode,Uniquemerchatid,merchantauthcode,ThemeId;
    PinePGPaymentManager objPinePGPaymentManager=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Amount = (EditText) findViewById(R.id.Amount);
        MerchantAuthCode = (EditText) findViewById((R.id.MerchantAuthCode));
        MerchantID = (EditText) findViewById(R.id.MerchantID);
        ProductCode = (EditText) findViewById((R.id.ProductCode));
        SecureSecretKey=(EditText) findViewById((R.id.Securesecretkey));
        UniqueMarchantID = (EditText) findViewById((R.id.UniqueMerchantTxnID));
        Production_Request=(RadioGroup) findViewById(R.id.ProducationRequest);
        Action_Bar=(RadioGroup) findViewById(R.id.Actionbar);
        ThemeID=(EditText) findViewById(R.id.Themeselection) ;
        Amount.setText(("550000"));
        MerchantAuthCode.setText("4e919e33-e940-49a7-a2ba-ece896b9800a");
        MerchantID.setText("2415");
        ProductCode.setText("40");
        SecureSecretKey.setText("840917F43186472EB2261ABCDE5CA576");
        InitializeListeners();
        InitializeView();
        InitializeObject();
    }



    public void Post(View view) {
        amount= String.valueOf(Amount.getText());
        ThemeId=String.valueOf(ThemeID.getText());
        Merchantid=String.valueOf(MerchantID.getText());
        Productcode= String.valueOf(ProductCode.getText());
        Uniquemerchatid=String.valueOf(UniqueMarchantID.getText());
        merchantauthcode= String.valueOf(MerchantAuthCode.getText());
        int ProductionRequestid = Production_Request.getCheckedRadioButtonId();
        int ActionBarID = Action_Bar.getCheckedRadioButtonId();
        productionRequestAction=(RadioButton) findViewById(ProductionRequestid);
        ActionBarAction=(RadioButton) findViewById(ActionBarID);
        try
        {
            String userEmail = "test@pinelabs.com";
            String userMobile = "9874563210";
            boolean isProductionRequest=false;
            boolean isHeaderTobeShow=false;
            if(productionRequestAction.getText().equals("Yes")) {
                isProductionRequest=true;
            }
            int iThemeIdVal=-1;
            if(ActionBarAction.getText().equals("Yes")) {
                isHeaderTobeShow=true;
            }
            if(!(ThemeId==null || ThemeId=="" || ThemeId.trim().length()==0))
            {
                iThemeIdVal=Integer.parseInt(ThemeId);
            }

            /*
            1.create order
            2.create pinepg payment param(hash map)
            3.create checksum/hash by calling merchant end server side utility (given by pine labs)
            4.add checksum as param in hashmap
            5.call pinelabs paymnent flow manager
            */

            Random random=new Random();
            final int min = 100;
            final int max = 999999999;
            final int randomNumber = new Random().nextInt((max - min) + 1) + min;
            Uniquemerchatid= String.valueOf(randomNumber)+"TEST APP";
            Order orderParam= createOrder(amount,Merchantid,Productcode,Uniquemerchatid,merchantauthcode);
            Map<String,String> pinePGPaymentParam= createPinePgPaymentParam(orderParam);
            String checksum=createCheckSum(pinePGPaymentParam);// this should be on server side(checksum util generation util given by pine labs)
            pinePGPaymentParam.put(PinePGConfig.PaymentParamsConstants.DIA_SECRET,checksum);
            pinePGPaymentParam.put(PinePGConfig.PaymentParamsConstants.DIA_SECRET_TYPE,"SHA256");

            objPinePGPaymentManager.startPayment(pinePGPaymentParam,this,iThemeIdVal,isHeaderTobeShow,isProductionRequest, new IPinePGResponseCallback()
            {



                @Override
                public void internetNotAvailable(int code, String message) {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onErrorOccured(int code, String message) {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }


                @Override
                public void onTransactionResponse() {
                    Toast.makeText(MainActivity.this, "Response received", Toast.LENGTH_LONG).show();
                    callTransactionStatusApi(Merchantid,Uniquemerchatid,merchantauthcode);

                }

                @Override
                public void onCancelTxn(int code, String message) {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onPressedBackButton(int code, String message) {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }
            });


        }catch(Exception ex)
        {

        }


    }

    public void callTransactionStatusApi(final String merchantid, final String uniqueMerchantTxnId,
                                         String merchantAccessCode)
    {

        PinePGService apiInterface = APIClient.getClient().create(PinePGService.class);


        /**
         GET List Resources
         **/
        Call<String> call = apiInterface.getTransactionStatus(merchantid,uniqueMerchantTxnId,merchantAccessCode);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {


                Log.d("TAG",response.code()+"");

                String displayResponse = "";
                String result = response.body();

                if(result.equalsIgnoreCase("SUCCESS")) {

                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Yeah! Your transaction is successful. \nMerchantId= "+ merchantid+" \nUnique Merchant Txn ID= "+uniqueMerchantTxnId+"")
                            .setTitle("SUCCESS")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                        }
                                    }).show();
                }
                else
                {
                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("OOPS! Your transaction is failed. \nMerchantId= "+ merchantid+" \nUnique Merchant Txn ID= "+uniqueMerchantTxnId+"")
                            .setTitle("FAILURE")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                        }
                                    }).show();
                }
                /*Integer text = resource.page;
                Integer total = resource.total;
                Integer totalPages = resource.totalPages;
                List<MultipleResource.Datum> datumList = resource.data;

                displayResponse += text + " Page\n" + total + " Total\n" + totalPages + " Total Pages\n";

                for (MultipleResource.Datum datum : datumList) {
                    displayResponse += datum.id + " " + datum.name + " " + datum.pantoneValue + " " + datum.year + "\n";
                }

                responseText.setText(displayResponse);*/

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void InitializeObject() {
        objPinePGPaymentManager=new PinePGPaymentManager();
    }

    private void InitializeView() {
    }

    private void InitializeListeners() {
    }

    //  @Override
    public void payNowClick(View v) {

    }

    private String createCheckSum(Map<String, String> pinePGPaymentParam) {
        //String secureSecretKey="08316C4FE4DD4CC887ED8A70B4B41E22";
        String secureSecretKey= String.valueOf(SecureSecretKey.getText());
        String secureType="SHA256";
        return HashingAlgorithm.GenerateHash(pinePGPaymentParam,secureSecretKey,secureType);
    }


    public  Order createOrder(String amount,String Merchantid,String Productcode,String Uniquemerchatid,String merchantauthcode) {



        Order paymentParamBuilder=new Order();
       /* paymentParamBuilder.setMerchantId(2415)
//                .setAmountInPaise(3000000)
//                .setMerchantAccessCode("58ad283b-7c93-4f19-b072-b17e8ecfb20e")
//                .setMerchantUrl(PinePGConfig.PINE_PG_PRETURN_URL_SANDBOX)
                .setMerchantAccessCode("4e919e33-e940-49a7-a2ba-ece896b9800a")
                .setAmountInPaise(550000)
                .setUniqueMerchantTxnId(String.valueOf(random.nextLong())+"TEST APP")
                .setNavigationMode(2)
                .setTransactionType(1)
                .setPayModeOnLandingPage("1,3,4,7")
                .setSequenceId(1)
                .setProductCode("40")
                .setCustomerEmail("harsh.kumar01@pinelabs.com")
                .setCustomerMobileNo("9582492891")
                .setCustomerId("786")
                .setCustomerAddress("hno 15")
                .setCustomerAddressPin("201301");*/

        paymentParamBuilder.setMerchantId(Integer.parseInt(Merchantid))
//                .setAmountInPaise(3000000)
//                .setMerchantAccessCode("58ad283b-7c93-4f19-b072-b17e8ecfb20e")
//                .setMerchantUrl(PinePGConfig.PINE_PG_PRETURN_URL_SANDBOX)
                .setMerchantAccessCode(merchantauthcode)
                .setAmountInPaise(Long.parseLong(amount))
                .setUniqueMerchantTxnId(Uniquemerchatid)
                .setNavigationMode(2)
                .setTransactionType(1)
                .setPayModeOnLandingPage("1,3,4,7")
                .setSequenceId(1)
                .setProductCode(Productcode)
                .setCustomerEmail("harsh.kumar01@pinelabs.com")
                .setCustomerMobileNo("9582492891")
                .setCustomerId("786")
                .setCustomerAddress("hno 15")
                .setCustomerAddressPin("201301");

        return paymentParamBuilder;

    }


    private  Map<String,String> createPinePgPaymentParam(Order objPaymentParam) {
        Map<String,String> hmPaymentParam=new HashMap<>();
        hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.UNIQUE_MERCHANT_TXN_ID ,objPaymentParam.getUniqueMerchantTxnId());
        hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.MERCHANT_ID ,String.valueOf(objPaymentParam.getMerchantId()));
        hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.AMOUNT ,String.valueOf(objPaymentParam.getAmountInPaise()));
        hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.MERCHANT_ACCESS_CODE  ,String.valueOf(objPaymentParam.getMerchantAccessCode()));
        hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.NAVIGATION_MODE  ,String.valueOf(objPaymentParam.getNavigationMode()));
        hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.TRANSACTION_TYPE  ,String.valueOf(objPaymentParam.getTransactionType()));
        hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.SEQUENCE_ID  ,String.valueOf(objPaymentParam.getSequenceId()));
        hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.PRODUCT_CODE  ,objPaymentParam.getProductCode());

        hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.PAYMENT_MODE_LKANDING_PAGE ,objPaymentParam.getPayModeOnLandingPage());
        hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.CUSTOMER_EMAIL  ,objPaymentParam.getCustomerEmail());
        hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.CUSTOMER_MOBILE_NO ,objPaymentParam.getCustomerMobileNo());
        hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.CUSTOMER_ID  ,objPaymentParam.getCustomerId());
        hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.CUSTOMER_ADDRESS ,objPaymentParam.getCustomerAddress());
        hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.CUSTOMER_ADDRESS_PIN  ,objPaymentParam.getCustomerAddressPin());
        //hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.MERCHANT_RETURN_URL  ,objPaymentParam.getMerchantUrl());
        //hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.REQUEST_AGENT  ,String.valueOf(objPaymentParam.getRequestAgent()));
        //hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.UDF_FIELD1  ,objPaymentParam.getUdfField1());
        // hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.UDF_FIELD2  ,objPaymentParam.getUdfField2());
        //hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.UDF_FIELD3  ,objPaymentParam.getUdfField3());
        //hmPaymentParam.put(PinePGConfig.PaymentParamsConstants.UDF_FIELD4  ,objPaymentParam.getUdfField4());

        return hmPaymentParam;


    }


}
