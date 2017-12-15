/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pns.kiam.sweb.controllers.telescope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.primefaces.event.RowEditEvent;
import pns.kiam.entities.telescopes.Telescope;
import pns.kiam.sweb.controllers.AbstractController;
import pns.kiam.sweb.controllers.telescope.utils.TelescopeUtils;

/**
 *
 * @author PSEVO tochka
 */
@Named
@Stateless
public class TelescopeController extends AbstractController implements Serializable {

    @EJB
    private TelescopeMaskController telescopeMaskController;

    private Telescope telescope;
    private List<Telescope> telescopeList = new ArrayList<>();
    private CriteriaQuery<Telescope> cq;

    private String selectedInfo = "";

    @PostConstruct
    public void initial() {
	try {
	    cb = em.getCriteriaBuilder();
	    cq = cb.createQuery(Telescope.class);
	    telescopeList = loadAllTelescopes();
	} catch (NullPointerException e) {
	}
    }

    public String getSelectedInfo() {
	return selectedInfo;
    }

    public Telescope getTelescope() {
	return telescope;
    }

    public void setTelescope(Telescope telescope) {
	this.telescope = telescope;
    }

    public List<Telescope> getTelescopeList() {
	return telescopeList;
    }

    public void setTelescopeList(List<Telescope> telescopeList) {
	this.telescopeList = telescopeList;
    }

    public void rowSelect() {
	try {
	    System.out.println(" RowSelect --- TELESCOPE ID " + telescope.getId());
	    telescopeMaskController.setTelescope(telescope);
	    telescopeMaskController.setSelectedInfo("");
	} catch (NullPointerException e) {
	}
    }

    public void rowSelectAction(Telescope t) {
	telescope = t;

	telescopeMaskController.setTelescope(telescope);
	selectedInfo = " ( Selected " + telescope.getIdentifier() + " telescope) ";
	TelescopeUtils.messageGenerator("Telescope Selected", telescope.toString());
	System.out.println("  Selected " + t);
    }

    public void rowDeSelect() {
	telescope = null;
	selectedInfo = "";
	telescopeMaskController.setTelescope(null);
    }

    public String longString(String L) {
	if (L == null) {
	    L = "0";
	}
	return TelescopeUtils.genRandomBigInt() + L;
    }

    /**
     * Removes the record(s) from Telescope table. if the "all " parameter is
     * true, removes all records else it is false removes only selected record
     * The represenation data in the lisr remove correspondently
     *
     * @param all
     */
    public void removeRow(boolean all) {
	System.out.println("  ALL " + all);
	deleteTelescope(all);
	rowDeSelect();
    }

    public void prepareCreation() {
	telescope = new Telescope();
	telescopeList.add(telescope);
	rowDeSelect();
	System.out.println("  create a new  telescope " + telescope);
    }

    public void onRowEdit(RowEditEvent event) {
	telescope = (Telescope) event.getObject();
	if (telescope.getId() == null) {
	    persist(telescope);
	} else {
	    merge(telescope);
	}
	telescopeList = loadAllTelescopes();
	rowDeSelect();
	TelescopeUtils.messageGenerator("Telescope Edited Result is:", ((Telescope) event.getObject()).toString());
//
//        FacesMessage msg = new FacesMessage("Telescope Edited Result is:", ((Telescope) event.getObject()).toString());
//        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowCancel(RowEditEvent event) {
	telescope = null;
	//    FacesMessage msg = new FacesMessage("Edit Cancelled", ((Car) event.getObject()).getId());
	//    FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    private List loadAllTelescopes() {

	Root<Telescope> res = cq.from(Telescope.class);
	cq.select(res);
	cq.orderBy(cb.asc(res.get("id")));
	TypedQuery<Telescope> Q = em.createQuery(cq);
	rowDeSelect();
	return Q.getResultList();
    }

    public void deleteTelescope(long id) {
	Telescope tt = em.find(Telescope.class, id);
	em.remove(tt);
    }

    public void deleteTelescope() {
	System.out.println("  Removing all telescopes ");
	for (int k = 0; k < telescopeList.size(); k++) {
	    Telescope tt = telescopeList.get(k);
	    deleteTelescope(tt.getId());
	}
    }

    /**
     * Removes the record(s) from Telescope table. if the "all " parameter is
     * true, removes all records else it is false removes only selected record
     *
     * @param all
     */
    private void deleteTelescope(boolean all) {
	if (all) {
	    System.out.println("Remove ALL");
	    telescopeList.clear();
	    deleteTelescope();
	    return;
	}
	System.out.println("tt " + telescope);
	if (telescope != null) {
	    System.out.println("  Remove telescope..." + telescope);
	    if (telescope.getId() != null) {
		deleteTelescope(telescope.getId());
		telescopeList.remove(telescope);
		telescope.getTelescopeMask().clear();
	    }
	}

	System.out.println(" TelescopeList Size:  " + telescopeList.size());
    }

}
