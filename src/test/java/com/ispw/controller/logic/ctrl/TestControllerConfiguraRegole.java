package com.ispw.controller.logic.ctrl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.ispw.BaseDAOTest;
import com.ispw.bean.DatiDisponibilitaBean;
import com.ispw.bean.EsitoOperazioneBean;
import com.ispw.bean.PenalitaBean;
import com.ispw.bean.RegolaCampoBean;
import com.ispw.bean.TempisticheBean;
import com.ispw.controller.logic.interfaces.disponibilita.GestioneDisponibilitaGestioneRegole;
import com.ispw.controller.logic.interfaces.manutenzione.GestioneManutenzioneConfiguraRegole;
import com.ispw.controller.logic.interfaces.notifica.GestioneNotificaConfiguraRegole;
import com.ispw.dao.factory.DAOFactory;
import com.ispw.dao.interfaces.CampoDAO;
import com.ispw.dao.interfaces.LogDAO;
import com.ispw.dao.interfaces.RegolePenalitaDAO;
import com.ispw.dao.interfaces.RegoleTempisticheDAO;
import com.ispw.model.entity.Campo;
import com.ispw.model.entity.RegolePenalita;
import com.ispw.model.entity.RegoleTempistiche;

@TestMethodOrder(MethodOrderer.DisplayName.class)
class TestControllerConfiguraRegole extends BaseDAOTest {

    private LogicControllerConfiguraRegole controller;

    private CampoDAO campoDAO;
    private RegoleTempisticheDAO tempDAO;
    private RegolePenalitaDAO penDAO;
    private LogDAO logDAO;

    @BeforeEach
    void setUp() {
        controller = new LogicControllerConfiguraRegole();

        campoDAO = DAOFactory.getInstance().getCampoDAO();
        tempDAO  = DAOFactory.getInstance().getRegoleTempisticheDAO();
        penDAO   = DAOFactory.getInstance().getRegolePenalitaDAO();
        logDAO   = DAOFactory.getInstance().getLogDAO();

        assertNotNull(campoDAO, "DAOFactory.getCampoDAO() ha restituito null");
        assertNotNull(tempDAO,  "DAOFactory.getRegoleTempisticheDAO() ha restituito null");
        assertNotNull(penDAO,   "DAOFactory.getRegolePenalitaDAO() ha restituito null");
        assertNotNull(logDAO,   "DAOFactory.getLogDAO() ha restituito null");

        tryClear(campoDAO);
        tryClear(tempDAO);
        tryClear(penDAO);
        tryClear(logDAO);
    }

    // =====================================================================================
    // 1) AggiornaRegoleCampo
    // =====================================================================================

    @Test
    @DisplayName("1) aggiornaRegoleCampo (overload): attivo=true & !manut → attivaDisponibilità + broadcast, no alert")
    void testAggiornaRegoleCampo_Attiva() {
        int idCampo = seedCampoAndGetId();

        RegolaCampoBean bean = new RegolaCampoBean();
        bean.setIdCampo(idCampo);
        bean.setAttivo(Boolean.TRUE);
        bean.setFlagManutenzione(Boolean.FALSE);

        FakeDisp disp = new FakeDisp();
        FakeMan  man  = new FakeMan();
        FakeNoti noti = new FakeNoti();

        EsitoOperazioneBean esito = controller.aggiornaRegoleCampo(bean, disp, man, noti);

        assertNotNull(esito);
        assertTrue(esito.isSuccess());

        assertEquals(1, disp.attivaCount);
        assertEquals(idCampo, disp.lastIdAttiva);
        assertEquals(0, disp.rimuoviCount);

        assertEquals(0, man.invocations);
        assertEquals(1, noti.invocations);

        Campo r = campoDAO.findById(idCampo);
        assertTrue(readBoolCompat(r, "isAttivo", "attivo"));
        assertFalse(readBoolCompat(r, "flagManutenzione", "manutenzione"));

        assertFalse(logDAO.findLast(1).isEmpty(), "Log append-only presente");
    }

    @Test
    @DisplayName("2) aggiornaRegoleCampo (overload): attivo=false → rimuoviDisponibilità + broadcast")
    void testAggiornaRegoleCampo_Disattiva() {
        int idCampo = seedCampoAndGetId();

        RegolaCampoBean bean = new RegolaCampoBean();
        bean.setIdCampo(idCampo);
        bean.setAttivo(Boolean.FALSE);
        bean.setFlagManutenzione(Boolean.FALSE);

        FakeDisp disp = new FakeDisp();
        FakeNoti noti = new FakeNoti();

        EsitoOperazioneBean esito = controller.aggiornaRegoleCampo(bean, disp, null, noti);

        assertNotNull(esito);
        assertTrue(esito.isSuccess());

        assertEquals(0, disp.attivaCount);
        assertEquals(1, disp.rimuoviCount);
        assertEquals(idCampo, disp.lastIdRimuovi);

        assertEquals(1, noti.invocations);

        Campo r = campoDAO.findById(idCampo);
        assertFalse(readBoolCompat(r, "isAttivo", "attivo"));
    }

    @Test
    @DisplayName("3) aggiornaRegoleCampo (overload): manut=true → rimuoviDisponibilità + alert manutentore + broadcast")
    void testAggiornaRegoleCampo_Manutenzione() {
        int idCampo = seedCampoAndGetId();

        RegolaCampoBean bean = new RegolaCampoBean();
        bean.setIdCampo(idCampo);
        bean.setAttivo(Boolean.TRUE);                // anche se attivo, manutenzione forza rimozione disponibilità
        bean.setFlagManutenzione(Boolean.TRUE);

        FakeDisp disp = new FakeDisp();
        FakeMan  man  = new FakeMan();
        FakeNoti noti = new FakeNoti();

        EsitoOperazioneBean esito = controller.aggiornaRegoleCampo(bean, disp, man, noti);

        assertNotNull(esito);
        assertTrue(esito.isSuccess());

        assertEquals(0, disp.attivaCount);
        assertEquals(1, disp.rimuoviCount);
        assertEquals(idCampo, man.lastCampoId);
        assertEquals(1, man.invocations);
        assertEquals(1, noti.invocations);

        Campo r = campoDAO.findById(idCampo);
        assertTrue(readBoolCompat(r, "flagManutenzione", "manutenzione"));
    }

    @Test
    @DisplayName("4) aggiornaRegoleCampo: KO validazione (idCampo<=0 o boolean null) → KO")
    void testAggiornaRegoleCampo_ValidazioneKO() {
        RegolaCampoBean b1 = new RegolaCampoBean();
        b1.setIdCampo(0);
        b1.setAttivo(Boolean.TRUE);
        b1.setFlagManutenzione(Boolean.FALSE);
        assertFalse(controller.aggiornaRegoleCampo(b1).isSuccess());

        RegolaCampoBean b2 = new RegolaCampoBean();
        b2.setIdCampo(1);
        b2.setAttivo(null);
        b2.setFlagManutenzione(Boolean.FALSE);
        assertFalse(controller.aggiornaRegoleCampo(b2).isSuccess());

        RegolaCampoBean b3 = new RegolaCampoBean();
        b3.setIdCampo(1);
        b3.setAttivo(Boolean.TRUE);
        b3.setFlagManutenzione(null);
        assertFalse(controller.aggiornaRegoleCampo(b3).isSuccess());
    }

    // =====================================================================================
    // 2) EseguiManutenzione
    // =====================================================================================

    @Test
    @DisplayName("5) eseguiManutenzione (base): forza manutenzione=true, disattiva campo, log presente")
    void testEseguiManutenzione_Base() {
        int idCampo = seedCampoAndGetId();

        RegolaCampoBean bean = new RegolaCampoBean();
        bean.setIdCampo(idCampo);

        EsitoOperazioneBean esito = controller.eseguiManutenzione(bean);

        assertNotNull(esito);
        assertTrue(esito.isSuccess());

        Campo r = campoDAO.findById(idCampo);
        assertFalse(readBoolCompat(r, "isAttivo", "attivo"));
        assertTrue(readBoolCompat(r, "flagManutenzione", "manutenzione"));

        assertFalse(logDAO.findLast(1).isEmpty());
    }

    @Test
    @DisplayName("6) eseguiManutenzione (overload): rimuoviDisponibilità + alert manutentore + broadcast")
    void testEseguiManutenzione_Overload() {
        int idCampo = seedCampoAndGetId();

        RegolaCampoBean bean = new RegolaCampoBean();
        bean.setIdCampo(idCampo);

        FakeDisp disp = new FakeDisp();
        FakeMan  man  = new FakeMan();
        FakeNoti noti = new FakeNoti();

        EsitoOperazioneBean esito = controller.eseguiManutenzione(bean, disp, man, noti);

        assertNotNull(esito);
        assertTrue(esito.isSuccess());

        assertEquals(1, disp.rimuoviCount);
        assertEquals(idCampo, disp.lastIdRimuovi);
        assertEquals(1, man.invocations);
        assertEquals(idCampo, man.lastCampoId);
        assertEquals(1, noti.invocations);
    }

    // =====================================================================================
    // 3) Aggiorna Regole Tempistiche
    // =====================================================================================

    @Test
    @DisplayName("7) aggiornaRegolaTempistiche (base): persiste in DAO + log")
    void testAggiornaRegolaTempistiche_Base() {
        TempisticheBean t = new TempisticheBean();
        t.setDurataSlotMinuti(60);
        t.setOraApertura(LocalTime.of(8, 0));
        t.setOraChiusura(LocalTime.of(22, 0));
        t.setPreavvisoMinimoMinuti(120);

        EsitoOperazioneBean esito = controller.aggiornaRegolaTempistiche(t);

        assertNotNull(esito);
        assertTrue(esito.isSuccess());

        RegoleTempistiche rt = tempDAO.get();
        assertNotNull(rt);
        assertEquals(60, rt.getDurataSlot());
        assertEquals(LocalTime.of(8, 0), rt.getOraApertura());
        assertEquals(LocalTime.of(22, 0), rt.getOraChiusura());
        assertEquals(120, rt.getPreavvisoMinimo());

        assertFalse(logDAO.findLast(1).isEmpty());
    }

    @Test
    @DisplayName("8) aggiornaRegolaTempistiche (overload): broadcast inviato")
    void testAggiornaRegolaTempistiche_Overload_Notifica() {
        TempisticheBean t = new TempisticheBean();
        t.setDurataSlotMinuti(45);
        t.setOraApertura(LocalTime.of(9, 0));
        t.setOraChiusura(LocalTime.of(21, 0));
        t.setPreavvisoMinimoMinuti(60);

        FakeNoti noti = new FakeNoti();

        EsitoOperazioneBean esito = controller.aggiornaRegolaTempistiche(t, noti);

        assertNotNull(esito);
        assertTrue(esito.isSuccess());
        assertEquals(1, noti.invocations);
    }

    @Test
    @DisplayName("9) aggiornaRegolaTempistiche: KO validazione (durata<=0, orari inval., preavviso<0)")
    void testAggiornaRegolaTempistiche_ValidazioneKO() {
        TempisticheBean t1 = new TempisticheBean();
        t1.setDurataSlotMinuti(0);
        t1.setOraApertura(LocalTime.of(8, 0));
        t1.setOraChiusura(LocalTime.of(22, 0));
        t1.setPreavvisoMinimoMinuti(10);
        assertFalse(controller.aggiornaRegolaTempistiche(t1).isSuccess());

        TempisticheBean t2 = new TempisticheBean();
        t2.setDurataSlotMinuti(60);
        t2.setOraApertura(LocalTime.of(10, 0));
        t2.setOraChiusura(LocalTime.of(9, 0)); // apertura non before chiusura
        t2.setPreavvisoMinimoMinuti(10);
        assertFalse(controller.aggiornaRegolaTempistiche(t2).isSuccess());

        TempisticheBean t3 = new TempisticheBean();
        t3.setDurataSlotMinuti(60);
        t3.setOraApertura(LocalTime.of(8, 0));
        t3.setOraChiusura(LocalTime.of(22, 0));
        t3.setPreavvisoMinimoMinuti(-1);
        assertFalse(controller.aggiornaRegolaTempistiche(t3).isSuccess());
    }

    // =====================================================================================
    // 4) Aggiorna Regole Penalità
    // =====================================================================================

    @Test
    @DisplayName("10) aggiornaRegolepenalita (base): persiste in DAO + log")
    void testAggiornaRegolePenalita_Base() {
        PenalitaBean p = new PenalitaBean();
        p.setValorePenalita(new BigDecimal("12.50"));
        p.setPreavvisoMinimoMinuti(90);

        EsitoOperazioneBean esito = controller.aggiornaRegolepenalita(p);

        assertNotNull(esito);
        assertTrue(esito.isSuccess());

        RegolePenalita rp = penDAO.get();
        assertNotNull(rp);
        assertEquals(new BigDecimal("12.50"), rp.getValorePenalita());
        assertEquals(90, rp.getPreavvisoMinimo());

        assertFalse(logDAO.findLast(1).isEmpty());
    }

    @Test
    @DisplayName("11) aggiornaRegolepenalita (overload): broadcast inviato")
    void testAggiornaRegolePenalita_Overload_Notifica() {
        PenalitaBean p = new PenalitaBean();
        p.setValorePenalita(new BigDecimal("5.00"));
        p.setPreavvisoMinimoMinuti(30);

        FakeNoti noti = new FakeNoti();

        EsitoOperazioneBean esito = controller.aggiornaRegolepenalita(p, noti);

        assertNotNull(esito);
        assertTrue(esito.isSuccess());
        assertEquals(1, noti.invocations);
    }

    @Test
    @DisplayName("12) aggiornaRegolepenalita: KO validazione (valore<=0, preavviso<0)")
    void testAggiornaRegolePenalita_ValidazioneKO() {
        PenalitaBean p1 = new PenalitaBean();
        p1.setValorePenalita(BigDecimal.ZERO);
        p1.setPreavvisoMinimoMinuti(30);
        assertFalse(controller.aggiornaRegolepenalita(p1).isSuccess());

        PenalitaBean p2 = new PenalitaBean();
        p2.setValorePenalita(new BigDecimal("-1"));
        p2.setPreavvisoMinimoMinuti(30);
        assertFalse(controller.aggiornaRegolepenalita(p2).isSuccess());

        PenalitaBean p3 = new PenalitaBean();
        p3.setValorePenalita(new BigDecimal("10"));
        p3.setPreavvisoMinimoMinuti(-5);
        assertFalse(controller.aggiornaRegolepenalita(p3).isSuccess());
    }

    // =====================================================================================
    // 5) Best-effort su collaboratori che lanciano eccezioni
    // =====================================================================================

    @Test
    @DisplayName("13) Overload: collaboratori lanciano eccezioni → operazione resta OK (best-effort)")
    void testOverload_BestEffortSuEccezioni() {
        int idCampo = seedCampoAndGetId();

        RegolaCampoBean beanCampo = new RegolaCampoBean();
        beanCampo.setIdCampo(idCampo);
        beanCampo.setAttivo(Boolean.TRUE);
        beanCampo.setFlagManutenzione(Boolean.FALSE);

        GestioneDisponibilitaGestioneRegole dispKo = new GestioneDisponibilitaGestioneRegole() {
            @Override public Boolean rimuoviDisponibilita(int id) { throw new RuntimeException("boom-rimuovi"); }
            @Override public List<DatiDisponibilitaBean> attivaDisponibilita(int id) { throw new RuntimeException("boom-attiva"); }
        };
        GestioneManutenzioneConfiguraRegole manKo = id -> { throw new RuntimeException("boom-man"); };
        GestioneNotificaConfiguraRegole notiKo = () -> { throw new RuntimeException("boom-noti"); };

        EsitoOperazioneBean e1 = controller.aggiornaRegoleCampo(beanCampo, dispKo, manKo, notiKo);
        assertNotNull(e1); assertTrue(e1.isSuccess(), "AggiornaRegoleCampo deve restare OK (best-effort)");

        RegolaCampoBean beanMan = new RegolaCampoBean();
        beanMan.setIdCampo(idCampo);
        EsitoOperazioneBean e2 = controller.eseguiManutenzione(beanMan, dispKo, manKo, notiKo);
        assertNotNull(e2); assertTrue(e2.isSuccess(), "EseguiManutenzione deve restare OK (best-effort)");

        TempisticheBean t = new TempisticheBean();
        t.setDurataSlotMinuti(30);
        t.setOraApertura(LocalTime.of(9, 0));
        t.setOraChiusura(LocalTime.of(10, 0));
        t.setPreavvisoMinimoMinuti(0);
        EsitoOperazioneBean e3 = controller.aggiornaRegolaTempistiche(t, notiKo);
        assertNotNull(e3); assertTrue(e3.isSuccess());

        PenalitaBean p = new PenalitaBean();
        p.setValorePenalita(new BigDecimal("2.50"));
        p.setPreavvisoMinimoMinuti(0);
        EsitoOperazioneBean e4 = controller.aggiornaRegolepenalita(p, notiKo);
        assertNotNull(e4); assertTrue(e4.isSuccess());
    }

    // =====================================================================================
    // Fake collaboratori secondari (DIP by-parameter)
    // =====================================================================================

    private static final class FakeDisp implements GestioneDisponibilitaGestioneRegole {
        int attivaCount;
        int rimuoviCount;
        Integer lastIdAttiva;
        Integer lastIdRimuovi;

        @Override
        public Boolean rimuoviDisponibilita(int idCampo) {
            rimuoviCount++;
            lastIdRimuovi = idCampo;
            return Boolean.TRUE;
        }

        @Override
        public List<DatiDisponibilitaBean> attivaDisponibilita(int idCampo) {
            attivaCount++;
            lastIdAttiva = idCampo;
            return Collections.emptyList();
        }
    }

    private static final class FakeMan implements GestioneManutenzioneConfiguraRegole {
        int invocations;
        Integer lastCampoId;

        @Override
        public void inviaAlertManutentore(int idCampo) {
            invocations++;
            lastCampoId = idCampo;
        }
    }

    private static final class FakeNoti implements GestioneNotificaConfiguraRegole {
        int invocations;

        @Override
        public void inviaNotificaAggiornamentoRegole() {
            invocations++;
        }
    }

    // =====================================================================================
    // Helper riflessivi / seed
    // =====================================================================================

    private static void tryClear(Object dao) {
        if (dao == null) return;
        try {
            Method m = dao.getClass().getMethod("clear");
            m.setAccessible(true);
            m.invoke(dao);
        } catch (ReflectiveOperationException ignored) { /* ignored: clear() may not be available for all DAO implementations */ }
    }

    /**
     * Crea un campo con ID esplicito (>0), lo salva e ritorna quell'ID.
     * Strategia robusta per In-Memory DAO:
     *  - se il DAO espone create(id), lo usiamo per ottenere un'istanza correttamente indirizzata
     *  - altrimenti settiamo l'ID sul Campo (setter o field) e poi store(...)
     *  - verifichiamo infine che findById(id) lo recuperi davvero
     */
    private int seedCampoAndGetId() {
        final int id = 10_001;

        // 1) Prova a usare un eventuale create(id) del DAO (via reflection, per non dipendere dai concreti)
        try {
            // Integer
            try {
                Method m = campoDAO.getClass().getMethod("create", Integer.class);
                Object created = m.invoke(campoDAO, Integer.valueOf(id));
                if (created instanceof Campo) {
                    // persist esplicito se necessario (alcuni create già salvano; altri richiedono store)
                    try { campoDAO.store((Campo) created); } catch (RuntimeException ignored) { /* ignored: best-effort store for in-memory DAO seeding */ }
                    Campo found = campoDAO.findById(id);
                    if (found != null) return id;
                }
            } catch (NoSuchMethodException ignore) {
                // prova con int
                try {
                    Method m = campoDAO.getClass().getMethod("create", int.class);
                    Object created = m.invoke(campoDAO, id);
                    if (created instanceof Campo) {
                        try { campoDAO.store((Campo) created); } catch (RuntimeException ignored) { /* ignored: best-effort store for in-memory DAO seeding */ }
                        Campo found = campoDAO.findById(id);
                        if (found != null) return id;
                    }
                } catch (NoSuchMethodException ignore2) {
                    // nessun create disponibile: passeremo al piano B
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // piano B
        }

        // 2) Piano B: costruiamo un Campo, settiamo l'ID e facciamo store(...)
        Campo c = new Campo();
        boolean setOk = false;
        try {
            // setter noti con int/Integer
            for (String mName : new String[]{"setIdCampo", "setId"}) {
                try {
                    Method m = null;
                    try { m = c.getClass().getMethod(mName, Integer.class); }
                    catch (NoSuchMethodException ex) { /* prova con int */ }
                    if (m == null) {
                        try { m = c.getClass().getMethod(mName, int.class); }
                        catch (NoSuchMethodException ex) { /* niente */ }
                    }
                    if (m != null) {
                        m.setAccessible(true);
                        // se prende Integer o int, invoca comunque con Integer (auto-unboxing se serve)
                        m.invoke(c, Integer.valueOf(id));
                        setOk = true;
                        break;
                    }
                } catch (ReflectiveOperationException ignored) { /* ignored: best-effort reflective access for test setup */ }
            }
            // field diretto "idCampo" (int/Integer)
            if (!setOk) {
                Field f = null; Class<?> k = c.getClass();
                while (k != null) {
                    try { f = k.getDeclaredField("idCampo"); break; }
                    catch (NoSuchFieldException ex) { k = k.getSuperclass(); }
                }
                if (f != null) {
                    f.setAccessible(true);
                    f.set(c, Integer.valueOf(id));
                    setOk = true;
                }
            }
        } catch (ReflectiveOperationException ignored) {
            // continueremo comunque
        }

        assertTrue(setOk, "Impossibile impostare l'ID del Campo nei test (manca setter/field compatibile)");

        campoDAO.store(c);

        Campo found = campoDAO.findById(id);
        assertNotNull(found, "CampoDAO non ha trovato il campo con l'ID esplicito settato nel test");
        return id;
    }

    /** Restituisce l'ID del campo provando getter noti o un eventuale field "idCampo". */
    private static int getCampoId(Campo c) {
        try {
            for (String mName : new String[]{"getIdCampo", "getId", "id"}) {
                try {
                    Method m = c.getClass().getMethod(mName);
                    Object v = m.invoke(c);
                    if (v instanceof Integer) return (int) v;
                } catch (ReflectiveOperationException ignored) { /* ignored: best-effort reflective access for test utils */ }
            }
            Field f = null; Class<?> k = c.getClass();
            while (k != null) {
                try { f = k.getDeclaredField("idCampo"); break; }
                catch (NoSuchFieldException ex) { k = k.getSuperclass(); }
            }
            if (f != null) { f.setAccessible(true); return (int) f.get(c); }
        } catch (ReflectiveOperationException ignored) { /* ignored: best-effort reflective access for test utils */ }
        return 0;
    }

    /** Legge un boolean compatibile provando lista di nomi di field e getter (isX/getX). */
    private static boolean readBoolCompat(Object target, String... candidateNames) {
        for (String n : candidateNames) {
            String base = capitalize(stripPrefix(n));
            for (String g : new String[]{"is" + base, "get" + base}) {
                try {
                    Method m = target.getClass().getMethod(g);
                    Object v = m.invoke(target);
                    if (v instanceof Boolean) return (Boolean) v;
                } catch (ReflectiveOperationException ignored) { /* ignored: method getter may be absent */ }
            }
        }
        for (String n : candidateNames) {
            try {
                Field f = null; Class<?> c = target.getClass();
                while (c != null) {
                    try { f = c.getDeclaredField(n); break; }
                    catch (NoSuchFieldException ex) { c = c.getSuperclass(); }
                }
                if (f != null) {
                    f.setAccessible(true);
                    Object v = f.get(target);
                    if (v instanceof Boolean) return (Boolean) v;
                }
            } catch (ReflectiveOperationException ignored) { /* ignored: field getter may be absent */ }
        }
        return false;
    }

    private static String stripPrefix(String s) {
        if (s == null) return "";
        if (s.startsWith("is")) return s.substring(2);
        return s;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
