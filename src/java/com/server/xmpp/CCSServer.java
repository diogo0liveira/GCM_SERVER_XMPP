package com.server.xmpp;

import com.server.xmpp.connection.Connect;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import static com.server.xmpp.util.Constants.API_KEY;
import static com.server.xmpp.util.Constants.SENDER_ID;

/**
 * @author Diogo Oliveira
 * @date 05/11/2015 09:42:21
 */
@Startup
@Singleton
public class CCSServer
{
    private static final Logger LOGGER = Logger.getLogger(CCSServer.class.getName());
    private static Connect SERVER;
    private Thread thread;

    public static synchronized Connect getInstance()
    {
        return SERVER;
    }

    @PostConstruct
    void initiate()
    {
        try
        {
            thread = new Thread(new CCSServer.startServer());
            thread.setName("START SERVER XMPP");
            thread.setDaemon(true);

            thread.start();
            thread.join();
        }
        catch(InterruptedException ex)
        {
            Logger.getLogger(CCSServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        LOGGER.log(Level.INFO, "[INITIATE]");
    }

    @PreDestroy
    void destroy()
    {
        if((SERVER != null) && SERVER.getConnection().isConnected())
        {
            SERVER.getConnection().disconnect();

            try
            {
                thread.interrupt();
                thread.join();
            }
            catch(InterruptedException ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        if(SERVER != null && SERVER.getConnection().isConnected())
        {
            SERVER.getConnection().disconnect();
        }

        LOGGER.log(Level.INFO, "[DESTROY]");
    }

    private JsonObject getParameters()
    {
        try(InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("/parameters/gcm-parameters-json.json"))
        {
            if(inputStream != null)
            {
                JsonReader jsonReader = Json.createReader(inputStream);
                inputStream.close();

                return jsonReader.readObject();
            }
            else
            {
                LOGGER.log(Level.SEVERE, "Pasta \"Web-INF\" não contem arquivo \"gcm-parameters-json.json\".");
            }
        }
        catch(IOException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }

        return null;
    }

    private class startServer implements Runnable
    {
        @Override
        public void run()
        {
            if((SERVER == null) || (!SERVER.getConnection().isConnected()))
            {
                JsonObject jsonObject = getParameters();

                if(jsonObject != null)
                {
                    if(jsonObject.containsKey(SENDER_ID) && jsonObject.containsKey(API_KEY))
                    {
                        try
                        {
                            SERVER = new Connect(jsonObject.getString(SENDER_ID), jsonObject.getString(API_KEY));
                        }
                        catch(SmackException | XMPPException | IOException ex)
                        {
                            LOGGER.log(Level.SEVERE, null, ex);
                        }
                    }
                    else
                    {
                        LOGGER.log(Level.SEVERE, "Parametros no arquivo \"gcm-parameters-json.json\" estão incorretos.");
                    }
                }
            }
        }
    }
}
