package com.server.app;

import com.gcm.server.http.Sender;
import com.google.gson.Gson;
import com.server.app.entity.AbstractFacade;
import com.server.app.entity.Device;
import com.server.xmpp.CCSServer;
import com.server.xmpp.GCMMessage;
import com.server.xmpp.MessageReceivedListener;
import com.server.xmpp.util.JsonKey;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.jivesoftware.smack.SmackException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import static com.gcm.server.http.Constants.INVALID_REGISTRATION;
import static com.server.app.util.Constants.GCM_API_KEY;

/**
 * @author Diogo Oliveira
 * @date 13/11/2015 10:12:22
 */
@Startup
@Singleton
@DependsOn("CCSServer")
public class MainApplication extends AbstractFacade<Device> implements MessageReceivedListener
{
    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    
    public MainApplication()
    {
        super(Device.class);
    }
    
    @PostConstruct
    void initiate()
    {
        CCSServer.getInstance().setMessageReceivedListener(this);
        
        entityManagerFactory = Persistence.createEntityManagerFactory("GCM_XMPPPU");
        entityManager = entityManagerFactory.createEntityManager();
    }
    
    @PreDestroy
    void destroy()
    {
        entityManager.close();
        entityManagerFactory.close();
    }
    
    @Override
    public void onMessageReceivedJson(JSONObject jSONObject)
    {
        if(jSONObject.containsKey(JsonKey.DATA))
        {
            JSONObject json = (JSONObject)JSONValue.parse(jSONObject.get(JsonKey.DATA).toString());
            
            if(json.containsKey(JsonKey.ACTION))
            {
                switch(json.get(JsonKey.ACTION).toString())
                {
                    case "register_user":
                    {
                        Gson gson = new Gson();
                        Device device = register(gson.fromJson(json.toString(), Device.class));
                        
                        try
                        {
                            GCMMessage message = GCMMessage.with(jSONObject.get(JsonKey.FROM), jSONObject.get(JsonKey.MESSAGE_ID))
                                    .setAction(json.get(JsonKey.ACTION).toString())
                                    .setData(JSONValue.parse(gson.toJson(device)));
                            
                            CCSServer.getInstance().sendMessage(message);
                        }
                        catch(SmackException.NotConnectedException ex)
                        {
                            Logger.getLogger(MainApplication.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        break;
                    }
                    default:
                    {
                        System.out.println(String.format("Tag \"action\" {0} não tratada.", json.get(JsonKey.ACTION)));
                        break;
                    }
                }
            }
        }
    }
    
    private Device register(Device device)
    {
        try
        {
            if(new Sender(GCM_API_KEY).checkingRegistrationId(device.getRegistrationId()))
            {
                TypedQuery<Device> queryExists = getEntityManager().createNamedQuery("Device.findByRegistrationId", Device.class);
                queryExists.setParameter("registrationId", device.getRegistrationId());
                
                if(queryExists.getResultList().isEmpty())
                {
                    /* Gera um novo Id */
                    TypedQuery<Integer> query = getEntityManager().createNamedQuery("Device.gerateId", Integer.class);
                    
                    device.setId(query.getSingleResult());
                    device.setRegistrationDate(Calendar.getInstance().getTimeInMillis());
                    create(device);
                }
                else
                {
                    device = queryExists.getSingleResult();
                }
            }
            else
            {
                device.setRegistrationId(INVALID_REGISTRATION);
            }
        }
        catch(IOException ex)
        {
            Logger.getLogger(MainApplication.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return device;
    }
    
    @Override
    protected EntityManager getEntityManager()
    {
        return entityManager;
    }
}
