/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client.skill;

public enum ExtendedSPType {
    EVAN,
    RESISTANCE;

    public static ExtendedSPType getFromJobID(int jobID) {
        if (jobID == 2001 || jobID / 100 == 22) {
            return EVAN;
        } else if (jobID / 1000 == 3) {
            return RESISTANCE;
        }
        return null;
    }
}
