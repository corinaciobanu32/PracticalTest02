package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;
import ro.pub.cs.systems.eim.practicaltest02.model.TimedKey;

public class CommunicationThread extends Thread {

    private final Socket socket;
    private final ServerThread serverThread;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.socket = socket;
        this.serverThread = serverThread;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!" + socket.getLocalPort());
            String operationType = bufferedReader.readLine();
            if (operationType == null || operationType.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                return;
            }
            String key = bufferedReader.readLine();
            if (key == null || key.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                return;
            }
            String value = "";
            if (operationType.equals(Constants.PUT)) {
                value = bufferedReader.readLine();
                if (value == null || value.isEmpty()) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                    return;
                }
            }

            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Client data..." + key + " " + operationType);
            HashMap<String, TimedKey> data = serverThread.getData();
            TimedKey timedKey = null;

            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
            HttpClient httpClient = new DefaultHttpClient();
            String pageSourceCode = "";
            HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS);
            HttpResponse httpGetResponse = httpClient.execute(httpGet);
            HttpEntity httpGetEntity = httpGetResponse.getEntity();
            if (httpGetEntity != null) {
                pageSourceCode = EntityUtils.toString(httpGetEntity);
            }

            if (pageSourceCode == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                return;
            } else
                Log.i(Constants.TAG, pageSourceCode);

            JSONObject content = new JSONObject(pageSourceCode);


            Integer time = content.getInt(Constants.UNIXTIME);
            Log.d(Constants.TAG, "[COMMUNICATION THREAD] Time :" + time);
            String result = null;
            if (operationType.equals(Constants.GET)) {
                Log.d(Constants.TAG, "[COMMUNICATION THREAD] GET :" + time + " " + key);
                if (data.containsKey(key)) {
                    if ((time -data.get(key).getTime()) < 20) {
                        Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                        result = data.get(key).getValue() +  "\n";
                        printWriter.println(result);
                        printWriter.flush();
                    }
                    else {
                        Log.d(Constants.TAG, "[COMMUNICATION THREAD]NO :" + time + " " + key);

                        result = "none\n";
                        printWriter.println(result);
                        printWriter.flush();
                    }
                }

            } else {
                Log.d(Constants.TAG, "[COMMUNICATION THREAD]PUT :" + value + " " + time);
                timedKey = new TimedKey(value, time);
                serverThread.setData(key, timedKey);

                if (timedKey == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Time Information is null!");
                    return;
                }
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        } catch (JSONException jsonException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
            if (Constants.DEBUG) {
                jsonException.printStackTrace();
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

}