package mpet.project2018.air.nfc;

import android.content.Context;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class NFCManager
{

    NfcAdapter nfcAdapterInstance;

    // Konstruktor kojim se kreira instanca nfc adaptera
    public NFCManager(Context appContext)
    {
        this.nfcAdapterInstance = NfcAdapter.getDefaultAdapter(appContext);
    }

    // Getter instance adaptera
    public NfcAdapter getNfcAdapterInstance()
    {
        return nfcAdapterInstance;
    }

    // Metoda za provjeru dostupnosti NFC značajke smartphone uređaja
    public boolean checkNFCAvailability()
    {
        if (nfcAdapterInstance != null && nfcAdapterInstance.isEnabled()) return true;
        else  return false;
    }

    // Metoda za validaciju NFC intenta
    public boolean isNFCIntent(Intent intent)
    {
        return intent.hasExtra(NfcAdapter.EXTRA_TAG);
    }

    // Metoda za dohvaćanje Ndef poruke iz pristiglog NFC intenta
    public NdefMessage getNdefMessageFromIntent(Intent intent)
    {
        NdefMessage ndefMessage = null;
        Parcelable[] extra = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (extra != null && extra.length > 0)
        {
            ndefMessage = (NdefMessage) extra[0];
        }
        return ndefMessage;
    }

    // Metoda za dohvaćanje prvog Ndef zapisa u Ndef poruci
    public NdefRecord getFirstNdefRecord(NdefMessage ndefMessage)
    {
        NdefRecord ndefRecord = null;
        NdefRecord[] ndefRecords = ndefMessage.getRecords();
        if (ndefRecords != null && ndefRecords.length > 0)
        {
            ndefRecord = ndefRecords[0];
        }
        return ndefRecord;
    }

    // Metoda provjere radil li se o ispravnom zapisu s obzirom na TNF i RDT svosjstva Ndef zapisa
    public boolean isNdefRecordOfTnfAndRdt(NdefRecord ndefRecord, short tnf, byte[] rdt)
    {
        return ndefRecord.getTnf() == tnf && Arrays.equals(ndefRecord.getType(), rdt);
    }

    // Metoda koja iz Ndef zapisa vraća kod kartice
    public String getCodeFromNdefRecord(NdefRecord ndefRecord)
    {
        String tagContent = null;
        try
        {
            byte[] payload = ndefRecord.getPayload();
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize + 1, payload.length - languageSize - 1, textEncoding);
        }
        catch (UnsupportedEncodingException e)
        {
            Log.e("getTextFromNdefRecord", e.getMessage(), e);
        }
        return tagContent;
    }

    // Metoda validacije ispravnog zapisa (koda) na kartici
    public boolean validateTag (Intent intent)
    {
        NdefMessage ndefMsg = getNdefMessageFromIntent(intent);
        if (ndefMsg != null)
        {
            NdefRecord ndefRecord = getFirstNdefRecord(ndefMsg);
            if (ndefRecord != null)
            {
                boolean isTextRecord = isNdefRecordOfTnfAndRdt(ndefRecord, NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT);
                if (isTextRecord)
                {
                    String tagContent = getCodeFromNdefRecord(ndefRecord);
                    if(tagContent.length()==10)
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    // Metoda za dohvat instance kartice
    public Tag getTag(Intent intent)
    {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        return tag;
    }

    // Metoda koja provjerava da li je kartica zaključana
    public boolean isLocked(Tag tag)
    {
        Ndef ndef=Ndef.get(tag);
        if(ndef.isWritable())
        {
            return true;
        }
        else return false;
    }


}