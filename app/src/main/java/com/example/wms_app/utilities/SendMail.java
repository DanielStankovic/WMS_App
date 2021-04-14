package com.example.wms_app.utilities;

import android.content.Context;
import android.os.AsyncTask;

import androidx.appcompat.app.AlertDialog;

import com.example.wms_app.enums.EnumMailContentType;

public class SendMail extends AsyncTask<Void, Void, Boolean> {

    private final Consumer mConsumer;
    private final AlertDialog loadingDialog;

    private final String body;
    private final String subject;
    private String mailTo = "";
    private final boolean addAttachment;
    private final EnumMailContentType mailContentType;
    private final String dbPath;

    public interface Consumer {
        void accept(Boolean sent);
    }

    public SendMail(Context context, String subject, String body, String mailTo,
                    boolean addAttachment, EnumMailContentType enumMailContentType,
                    Consumer consumer) {
        this.subject = subject;
        this.body = body;
        this.mailTo = mailTo;
        this.addAttachment = addAttachment;
        this.mailContentType = enumMailContentType;
        loadingDialog = DialogBuilder.getLoadingDialog(context);
        dbPath = context.getDatabasePath(Constants.DB_NAME).getAbsolutePath();
        mConsumer = consumer;
        execute();
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        loadingDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            Mail m = new Mail("office@micro-bs.com", "softver!");
            String[] toArr = {mailTo, "filip.blagojevic@micro-bs.com", "kolibri@micro-bs.com", "daniel.stankovic@micro-bs.com"};
            //String[] toArr = {"daniel.stankovic@micro-bs.com"};
            m.setTo(toArr);
            m.setFrom("office@micro-bs.com");
            m.setSubject(subject);
            m.setBody(body);
            m.setMailContentType(mailContentType);
            if (addAttachment && Utility.isErrorLogFileExists()) {
                m.addAttachment(Utility.getErrorLogPath() + Constants.ERROR_FILE);
            }

            if (dbPath != null && !dbPath.isEmpty()) {
                boolean dbExported = Utility.exportDB(dbPath);
                if (dbExported) {
                    m.addAttachment(Utility.getDatabasePath() + Constants.DB_NAME + ".db");
                }
            }

            return m.send();
        } catch (Exception e) {
            Utility.writeErrorToFile(e);
            return false;
        }

    }

    @Override
    protected void onPostExecute(Boolean sent) {
        if (loadingDialog != null && loadingDialog.isShowing())
            loadingDialog.dismiss();

        if (addAttachment && sent) {
            Utility.deleteErrorLogFile();
        }
        mConsumer.accept(sent);
    }

}