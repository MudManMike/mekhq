/*
 * MekHQ - Copyright (C) 2017 - The MegaMek Team
 *
 *  This program is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation; either version 2 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 */
package mekhq.campaign.personnel;

import java.util.Enumeration;
import java.util.Hashtable;

import megamek.common.logging.LogLevel;
import megamek.common.options.AbstractOptionsInfo;
import megamek.common.options.IBasicOptionGroup;
import megamek.common.options.IOptionInfo;
import megamek.common.options.OptionsConstants;
import megamek.common.options.PilotOptions;
import mekhq.MekHQ;

/**
 * An extension of PilotOptions that adds MekHQ-specific SPAs and edge triggers for support and command
 * actions. Display names and descriptions are taken from SpecialAbility when present, otherwise
 * from the MM option.
 * 
 * @author Neoancient
 *
 */
public class PersonnelOptions extends PilotOptions {
    
    /**
     * 
     */
    private static final long serialVersionUID = -4376899952366335620L;
    
    public static final String EDGE_MEDICAL = "edge_when_heal_crit_fail";
    public static final String EDGE_REPAIR_BREAK_PART = "edge_when_repair_break_part";
    public static final String EDGE_REPAIR_FAILED_REFIT = "edge_when_fail_refit_check";
    public static final String EDGE_ADMIN_ACQUIRE_FAIL = "edge_when_admin_acquire_fail";
    
    @Override
    public void initialize() {
        final String METHOD_NAME = "initialize()"; //$NON-NLS-1$
        super.initialize();

        IBasicOptionGroup edge = null;
        for (Enumeration<IBasicOptionGroup> e = getOptionsInfoImp().getGroups(); e.hasMoreElements(); ) {
            final IBasicOptionGroup group = e.nextElement();
            if (group.getKey().equals(PilotOptions.EDGE_ADVANTAGES)) {
                edge = group;
                break;
            }
        }
        
        if (null == edge) {
            // This really shouldn't happen.
            MekHQ.getLogger().log(PersonnelOptions.class, METHOD_NAME,
                    LogLevel.WARNING, "Could not find edge group"); //$NON-NLS-1$
            edge = addGroup("edge", PilotOptions.EDGE_ADVANTAGES); // $NON-NLS-1$
            addOption(edge, OptionsConstants.EDGE, 0);
        }
        
        // Add MekHQ-specific options
        addOption(edge, EDGE_MEDICAL, false);
        addOption(edge, EDGE_REPAIR_BREAK_PART, false);
        addOption(edge, EDGE_REPAIR_FAILED_REFIT, false);
        addOption(edge, EDGE_ADMIN_ACQUIRE_FAIL, false);
    }

    /* 
     * When an option is added we need to create a custom IOptionInfo instance so we can
     * provide a different source for display name and description.
     */
    @Override
    protected void addOption(IBasicOptionGroup group, String name, int type,
            Object defaultValue) {
        super.addOption(group, name, type, defaultValue);
        ((PersonnelOptionsInfo)getOptionsInfoImp()).setOptionInfo(name);
    }

    @Override
    protected AbstractOptionsInfo getOptionsInfoImp() {
        return PersonnelOptionsInfo.getInstance();
    }

    /**
     * Custom IOptionsInfo class that allows adding additional options to the base MegaMek
     * options before finalizing and also holds a hash of IOptionInfo objects for the abilities
     * so we can provide names and descriptions for the MekHQ-specific options.
     * 
     * @author Neoancient
     *
     */
    private static class PersonnelOptionsInfo extends AbstractOptionsInfo {
        private static boolean initialized = false;
        private static AbstractOptionsInfo instance = new PersonnelOptionsInfo();

        private Hashtable<String, IOptionInfo> optionsHash = new Hashtable<>();

        public static AbstractOptionsInfo getInstance() {
            if (!initialized) {
                initialized = true;
                // Create a new dummy PilotOptions; ensures values initialized
                // Otherwise, could have issues when loading saved games
                new PersonnelOptions();
            }
            return instance;
        }

        protected PersonnelOptionsInfo() {
            super("PersonnelOptionsInfo"); //$NON-NLS-1$
        }
        
        public IOptionInfo getOptionInfo(String name) {
            return optionsHash.get(name);
        }

        private void setOptionInfo(String name) {
            optionsHash.put(name, new PersonnelOptionInfo(name));
        }
    }
    
    /**
     * Access to ability names and descriptions from <code>SpecialAbility</code> if the ability
     * has an entry, otherwise checks for the ability the MM PilotOptions class. If not found
     * in either place, returns the lookup key instead.
     * 
     * @author Neoancient
     *
     */
    private static class PersonnelOptionInfo implements IOptionInfo {
        
        private String name;
        private static PilotOptions mmOptions = new PilotOptions();
        
        public PersonnelOptionInfo(String name) {
            this.name = name;
        }

        @Override
        public String getDisplayableName() {
            if (null != SpecialAbility.getAbility(name)) {
                return SpecialAbility.getAbility(name).getDisplayName();
            } else if (null != SpecialAbility.getEdgeTrigger(name)) {
                return SpecialAbility.getEdgeTrigger(name).getDisplayName();
            } else if (null != mmOptions.getOption(name)){
                return mmOptions.getOption(name).getDisplayableName();
            } else {
                return name;
            }
        }

        @Override
        public String getDisplayableNameWithValue() {
            if (null != SpecialAbility.getAbility(name)) {
                return SpecialAbility.getAbility(name).getDisplayName();
            } else if (null != SpecialAbility.getEdgeTrigger(name)) {
                return SpecialAbility.getEdgeTrigger(name).getDisplayName();
            } else if (null != mmOptions.getOption(name)){
                return mmOptions.getOption(name).getDisplayableName();
            } else {
                return name;
            }
        }

        @Override
        public String getDescription() {
            if (null != SpecialAbility.getAbility(name)) {
                return SpecialAbility.getAbility(name).getDescription();
            } else if (null != SpecialAbility.getEdgeTrigger(name)) {
                return SpecialAbility.getEdgeTrigger(name).getDescription();
            } else if (null != mmOptions.getOption(name)){
                return mmOptions.getOption(name).getDescription();
            } else {
                return name;
            }
        }

        @Override
        public int getTextFieldLength() {
            return 3;
        }

        @Override
        public boolean isLabelBeforeTextField() {
            return false;
        }
        
    }
}
