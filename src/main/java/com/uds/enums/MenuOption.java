package com.uds.enums;
public enum MenuOption {
    EMPTY (-1),
    QUIT  (0),
    SWITCH_STRATEGY (1),
    CREATE_STRATEGY (2),
    SIMULATE_TRANSACTION (3);

    private final int selection;
    MenuOption(int selection) {
        this.selection = selection;
    }

    public static MenuOption fromInt(int selection) {
        for ( final MenuOption option : MenuOption.values() ) {
            if ( option.selection == selection ) {
                return option;
            }
        }

        return MenuOption.EMPTY;
    }

    public int getSelection() {
        return this.selection;
    }
}