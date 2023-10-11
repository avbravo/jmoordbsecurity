/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.avbravo.jmoordbsecurity;


import com.avbravo.jmoordbsecurity.localutils.JsfUtilSecurity;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author avbravo
 */
@WebListener
public class SessionListener implements HttpSessionListener {
//public class SessionListener implements HttpSessionListener, HttpSessionAttributeListener {

    private static int numberOfSession = 0;
    private static List<BrowserSession> browserSessionList = new ArrayList<>();

    // <editor-fold defaultstate="collapsed" desc="get/set"> 
    public static List<BrowserSession> getBrowserSessionList() {
        return browserSessionList;
    }

    public static void setBrowserSessionList(List<BrowserSession> browserSessionList) {
        SessionListener.browserSessionList = browserSessionList;
    }

    public static int getNumberOfSession() {
        return numberOfSession;
    }
//
//    public static int getMaximosSegundosInactividad() {
//        return maximosSegundosInactividad;
//    }
//
//    public static void setMaximosSegundosInactividad(int maximosSegundosInactividad) {
//        SessionListener.maximosSegundosInactividad = maximosSegundosInactividad;
//    }

    public static void setNumberOfSession(int numberOfSession) {
        SessionListener.numberOfSession = numberOfSession;
    }

    public void attributeAdded(HttpSessionBindingEvent arg0) {

        //    System.out.println("value is added ");
    }

    public void attributeRemoved(HttpSessionBindingEvent arg0) {
        try {

            System.out.println("value is removed");

        } catch (Exception e) {
            JsfUtilSecurity.errorMessage("attributeRemoved() " + e.getLocalizedMessage());
        }

    }

    public void attributeReplaced(HttpSessionBindingEvent arg0) {
//        try {

            System.out.println("value has been replaced " + JsfUtilSecurity.getTiempo());

//            System.out.println("getId() " + arg0.getSession().getId());
//
//
//            Long consumidos = (JsfUtilSecurity.fechaActualEnMilisegundos() - arg0.getSession().getCreationTime()) / 1000;
//            
//            HttpSession httpSession = arg0.getSession();
//            Integer nuevoMaximoInactividad = 0;
//            for (BrowserSession b : browserSessionList) {
//                if (b.getId().equals(httpSession.getId())) {
//
//                    nuevoMaximoInactividad = b.getMaxSgundosParaInactividad() + consumidos.intValue();
//                    b.getSession().setMaxInactiveInterval(nuevoMaximoInactividad);
//                    arg0.getSession().setMaxInactiveInterval(nuevoMaximoInactividad);
//                    HttpSession session = arg0.getSession();
//                    session.setMaxInactiveInterval(nuevoMaximoInactividad);
//                    break;
//                }
//            }
//
//            System.out.println("++++++++++++++++++++++++++++++++");
//
//        } catch (Exception e) {
//            JsfUtilSecurity.errorMessage("attributeReplaced() " + e.getLocalizedMessage());
//        }

    }


    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="SessionListener"> 
    public SessionListener() {
        System.out.println("call SessionListener a las " + JsfUtilSecurity.getTiempo());

        numberOfSession = 0;
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="inicializar"> 
    public static void inicializar() {

        browserSessionList = new ArrayList<>();
        numberOfSession = 0;
    } // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="sessionCreated"> 
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSession session = se.getSession();

        session.setMaxInactiveInterval(2100);
        session.setAttribute("id", session.getId());
        LocalTime time = JsfUtilSecurity.getTiempo();
        session.setAttribute("time", time);
        session.setAttribute("ipcliente", JsfUtilSecurity.getIp());
        session.setAttribute("browser", JsfUtilSecurity.getBrowserName());
        synchronized (this) {
            numberOfSession++;
        }

        //BrowserSession browserSession = new BrowserSession(session.getId(), time, JsfUtilSecurity.getIp(), JsfUtilSecurity.getBrowserName(), "", "", 0);
        BrowserSession browserSession = new BrowserSession(session.getId(), time, JsfUtilSecurity.getIp(), JsfUtilSecurity.getBrowserName(), "", "", session, numberOfSession);
        browserSessionList.add(browserSession);

       // JsfUtilSecurity.successMessage("Se creo una sesion " + session.getId());

    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="sessionDestroyed"> 
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        session.setMaxInactiveInterval(15);

        synchronized (this) {
            if (numberOfSession > 0) {
                numberOfSession--;
            }
        }

        Boolean found = false;

        //Voy a renoverlo del browser
        if (removeBrowserSession(session)) {
//            System.out.println("!!! quitandolo del browserSessionList");
        } else {
//            System.out.println("!!! No se quitandolo del browserSessionList");
        }

    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="removeBrowserSession"> 
    private Boolean removeBrowserSession(HttpSession session) {
        Boolean found = false;
        try {

            for (BrowserSession p : browserSessionList) {
                if (p.getId().equals(session.getId())) {
                    browserSessionList.remove(p);
                    //  System.out.println("!!! quitandolo del browserSessionList");
                    found = true;
                    break;
                }
            }
        } catch (Exception e) {
            JsfUtilSecurity.errorMessage("removeBrowserSession" + e.getLocalizedMessage());
        }
        return found;
    }// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="isUserLoged"> 
    public static Boolean isUserLogged(String username) {
        Boolean found = false;
        try {
            for (BrowserSession browserSession : browserSessionList) {
                if (browserSession.getUsername().equals(username)) {
                    found = true;
                    break;
                }

            }

        } catch (Exception e) {
            JsfUtilSecurity.errorMessage("isUserLoged " + e.getLocalizedMessage());
        }
        return found;
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="addUsername"> 
    /**
     * agrega el username logeado a la lista
     *
     * @param username
     * @return
     */
    public static Boolean addUsername(String username, HttpSession session, String token, Integer maxSegundosParaInactividad) {
        Boolean add = false;
        try {
            if (isUserLogged(username)) {
                return false;
            }

            Integer c = 0;

            for (BrowserSession p : browserSessionList) {
                if (p.getId().equals(session.getId())) {
                    browserSessionList.get(c).setUsername(username);
                    browserSessionList.get(c).setToken(token);
                    browserSessionList.get(c).setMaxSgundosParaInactividad(maxSegundosParaInactividad);
                }
                c++;
            }

            // System.out.println("---> Agregado a la sesion" + username);
        } catch (Exception e) {
            JsfUtilSecurity.errorMessage("addUsername " + e.getLocalizedMessage());
        }
        return add;
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="killAllSesion()">  
// <editor-fold defaultstate="collapsed" desc="killAllSesion">  
    /**
     * mata todas las sesiones
     *
     * @return
     */
    public static Boolean cancelAllSesion() {
        try {
            for (BrowserSession browserSession : browserSessionList) {

                browserSession.getSession().invalidate();
            }

            inicializar();
            return true;
        } catch (Exception e) {
            JsfUtilSecurity.errorMessage("killAllSesion()" + e.getLocalizedMessage());
        }
        return false;
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="inactiveSession"> 
    public static Boolean inactiveSession(BrowserSession browserSession) {
        try {
            if (browserSession.session == null) {
                return true;
            }
            browserSession.session.invalidate();
            browserSessionList.remove(browserSession);
            return true;
        } catch (Exception e) {
            JsfUtilSecurity.errorMessage("inactiveSession() " + e.getLocalizedMessage());
        }
        return false;
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="inactiveSessionByToken"> 
 /**
  * inactiveSessionByToken
  * @param token
  * @param username
  * @return 
  */   
    public static Boolean inactiveSessionByToken(String token, String username) {
        try {
            for (BrowserSession b : browserSessionList) {
                if (b.session != null) {
                    if (b.getToken().equals(token) && b.getUsername().equals(username)) {
                        b.session.invalidate();
                        browserSessionList.remove(b);
                        return true;
                    }

                }
            }

            return false;
        } catch (Exception e) {
            JsfUtilSecurity.errorMessage("inactiveSessionByToken() " + e.getLocalizedMessage());
        }
        return false;
    }
// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="inactiveSessionByUsername"> 
 /**
  * inactiveSessionByToken
  * @param token
  * @param username
  * @return 
  */   
    public static Boolean inactiveSessionByUsername( String username) {
        try {
            for (BrowserSession b : browserSessionList) {
                if (b.session != null) {
                    if (b.getUsername().equals(username)) {
                        b.session.invalidate();
                        browserSessionList.remove(b);
                        return true;
                    }

                }
            }

            return false;
        } catch (Exception e) {
            JsfUtilSecurity.errorMessage("inactiveSessionByUsername() " + e.getLocalizedMessage());
        }
        return false;
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="isUsernameHaveSession">  
    /**
     * indica si tiene un session con ese username
     *
     * @param username
     * @return
     */
    public static Boolean isUsernameHaveSession(String username) {
        Boolean found = false;
        try {
            for (BrowserSession browserSession : browserSessionList) {
                if (username.equals(browserSession.getUsername())) {
                    found = true;
                    break;
                }
            }

        } catch (Exception e) {
            JsfUtilSecurity.errorMessage("usernameHaveSession()" + e.getLocalizedMessage());
        }
        return found;
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="sesionOfUsername">  
    /**
     * devuelve el session de un username
     *
     * @param username
     * @return
     */
    public static HttpSession sesionOfUsername(String username) {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession();
        try {
            for (BrowserSession browserSession : browserSessionList) {
                if (username.equals(browserSession.getUsername())) {
                    session = browserSession.getSession();
                    break;
                }

            }
        } catch (Exception e) {
            JsfUtilSecurity.errorMessage("sesionOfUsername()" + e.getLocalizedMessage());
        }
        return session;
    }// </editor-fold>

}
