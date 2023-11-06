/*
 * THE SOURCE CODE AND ITS RELATED DOCUMENTATION IS PROVIDED "AS IS". INFINEON
 * TECHNOLOGIES MAKES NO OTHER WARRANTY OF ANY KIND,WHETHER EXPRESS,IMPLIED OR,
 * STATUTORY AND DISCLAIMS ANY AND ALL IMPLIED WARRANTIES OF MERCHANTABILITY,
 * SATISFACTORY QUALITY, NON INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 *
 * THE SOURCE CODE AND DOCUMENTATION MAY INCLUDE ERRORS. INFINEON TECHNOLOGIES
 * RESERVES THE RIGHT TO INCORPORATE MODIFICATIONS TO THE SOURCE CODE IN LATER
 * REVISIONS OF IT, AND TO MAKE IMPROVEMENTS OR CHANGES IN THE DOCUMENTATION OR
 * THE PRODUCTS OR TECHNOLOGIES DESCRIBED THEREIN AT ANY TIME.
 *
 * INFINEON TECHNOLOGIES SHALL NOT BE LIABLE FOR ANY DIRECT, INDIRECT OR
 * CONSEQUENTIAL DAMAGE OR LIABILITY ARISING FROM YOUR USE OF THE SOURCE CODE OR
 * ANY DOCUMENTATION, INCLUDING BUT NOT LIMITED TO, LOST REVENUES, DATA OR
 * PROFITS, DAMAGES OF ANY SPECIAL, INCIDENTAL OR CONSEQUENTIAL NATURE, PUNITIVE
 * DAMAGES, LOSS OF PROPERTY OR LOSS OF PROFITS ARISING OUT OF OR IN CONNECTION
 * WITH THIS AGREEMENT, OR BEING UNUSABLE, EVEN IF ADVISED OF THE POSSIBILITY OR
 * PROBABILITY OF SUCH DAMAGES AND WHETHER A CLAIM FOR SUCH DAMAGE IS BASED UPON
 * WARRANTY, CONTRACT, TORT, NEGLIGENCE OR OTHERWISE.
 *
 * (C)Copyright INFINEON TECHNOLOGIES All rights reserved
 */

package com.infineon.esim.lpa.lpa;

import android.net.ConnectivityManager;
import android.net.Network;

import androidx.annotation.NonNull;

import com.infineon.esim.lpa.Application;
import com.infineon.esim.lpa.util.android.InternetConnectionConsumer;
import com.infineon.esim.lpa.util.android.NetworkStatus;
import com.infineon.esim.util.Log;

public class NetworkStatusBroadcastReceiver {
    private static final String TAG = NetworkStatusBroadcastReceiver.class.getName();

    private final InternetConnectionConsumer internetConnectionConsumer;

    public NetworkStatusBroadcastReceiver(InternetConnectionConsumer internetConnectionConsumer) {
        this.internetConnectionConsumer = internetConnectionConsumer;

        // Initiate internet connection consumer
        if(NetworkStatus.isNetworkAvailable()) {
            internetConnectionConsumer.onConnected();
        } else {
            internetConnectionConsumer.onDisconnected();
        }
    }

    public void registerReceiver() {
        ConnectivityManager connectivityManager = Application.getConnectivityManager();
        connectivityManager.registerDefaultNetworkCallback(networkCallback);
    }

    public void unregisterReceiver() {
        ConnectivityManager connectivityManager = Application.getConnectivityManager();
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            Log.debug(TAG, "Network available!");
            super.onAvailable(network);
            internetConnectionConsumer.onConnected();
        }

        @Override
        public void onLost(@NonNull Network network) {
            Log.debug(TAG, "Network lost!");
            super.onLost(network);
            internetConnectionConsumer.onDisconnected();
        }

        @Override
        public void onUnavailable() {
            Log.debug(TAG, "Network unavailable!");
            super.onUnavailable();
            internetConnectionConsumer.onDisconnected();
        }
    };
}
