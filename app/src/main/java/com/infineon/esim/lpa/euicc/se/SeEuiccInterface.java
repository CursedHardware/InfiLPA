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

package com.infineon.esim.lpa.euicc.se;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.infineon.esim.lpa.Application;
import com.infineon.esim.lpa.euicc.base.EuiccConnection;
import com.infineon.esim.lpa.euicc.base.EuiccInterface;
import com.infineon.esim.lpa.euicc.base.EuiccInterfaceStatusChangeHandler;
import com.infineon.esim.util.Log;

import java.util.ArrayList;
import java.util.List;

final public class SeEuiccInterface implements EuiccInterface {
    private static final String TAG = SeEuiccInterface.class.getName();

    public static final String INTERFACE_TAG = "SE";

    private final SeService seService;
    private final List<String> euiccNames;

    private EuiccConnection euiccConnection;

    public SeEuiccInterface(Context context, EuiccInterfaceStatusChangeHandler euiccInterfaceStatusChangeHandler) {
        Log.debug(TAG, "Constructor of SeEuiccReader.");

        this.seService = new SeService(context, euiccInterfaceStatusChangeHandler);
        this.euiccNames = new ArrayList<>();
    }

    @Override
    public String getTag() {
        return INTERFACE_TAG;
    }

    @Override
    public boolean isAvailable() {
        PackageManager packageManager = Application.getPacketManager();
        boolean isAvailable = false;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // Starting with Android R the existence of OMAPI can be checked
            isAvailable = packageManager.hasSystemFeature(PackageManager.FEATURE_SE_OMAPI_UICC);
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Starting with Android P the OMAPI is part of Android but cannot be checked (yet).
            isAvailable = true;
        }

        Log.debug(TAG, "Checking if SE eUICC interface is available: " + isAvailable);

        return isAvailable;
    }

    @Override
    public boolean isInterfaceConnected() {
        boolean isConnected = false;
        if(isAvailable()) {
            isConnected = seService.isConnected();
        }

        return isConnected;
    }

    @Override
    public boolean connectInterface() throws Exception {
        seService.connect();

        return seService.isConnected();
    }

    @Override
    public boolean disconnectInterface() throws Exception {

        if(euiccConnection != null) {
            euiccConnection.close();
            euiccConnection = null;
        }

        if(seService != null) {
            seService.disconnect();
        }

        euiccNames.clear();

        return true;
    }

    @Override
    public List<String> refreshEuiccNames() {
        Log.debug(TAG, "Refreshing SE eUICC names...");
        euiccNames.clear();
        
        if(isInterfaceConnected()) {
            euiccNames.addAll(seService.refreshEuiccNames());
        }

        return euiccNames;
    }

    @Override
    public List<String> getEuiccNames() {
        return euiccNames;
    }

    @Override
    public EuiccConnection getEuiccConnection(String euiccName) throws Exception {

        if(isNotYetOpen(euiccName)) {
            // Close the old eUICC connection if it is with another eUICC
            if(euiccConnection != null) {
                euiccConnection.close();
            }

            // Open new eUICC connection
            euiccConnection = seService.openEuiccConnection(euiccName);
        }

        return euiccConnection;
    }

    private boolean isNotYetOpen(String euiccName) {
        if(euiccConnection == null) {
            return true;
        } else {
            return !euiccConnection.getEuiccName().equals(euiccName);
        }
    }
}
