package com.example.gesturecontrolapplication.util;

public class Util {
        private Util() {

        }


        public static String getVideoName(String Name) {
            if(Name.equalsIgnoreCase("Turn On Light")) return "lighton";
            if(Name.equalsIgnoreCase( "Turn Off Light")) return "lightoff";
            if(Name.equalsIgnoreCase("Turn On Fan")) return "fanon";
            if(Name.equalsIgnoreCase("Turn Off Fan")) return "fanoff";
            if(Name.equalsIgnoreCase("Increase Fan Speed")) return "fanup";
            if(Name.equalsIgnoreCase( "Decrease Fan Speed")) return "fandown";
            if(Name.equalsIgnoreCase("Set Thermostat to specified temperature")) return "setthermo";
            else {
                return "a"+Name;
            }

        }
}
