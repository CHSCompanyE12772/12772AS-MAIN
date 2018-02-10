package org.firstinspires.ftc.teamcode.competitioncode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.Disabled; // Leave this line here even when not used, please

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuMarkInstanceId;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import java.util.Arrays;


/**
 * Autonomous OP mode to identify and act using vuforia mark identification, parent to each corner's OP mode.
 */

@Autonomous(name = "AutoOD Vuforia Body", group = "OD")
//@Disabled                            //Enables or disables such OpMode (hide or show on Driver Station OpMode List)
public class AutoODVuforia extends LinearOpMode {

    Hardware_OD_OmniDirection r = new Hardware_OD_OmniDirection();
    General12772 g = new General12772(); //Use the shared general robot code.

    VuforiaLocalizer vuforia;   //Variable is a reference to the instance of the Vuforia localization/tracking engine

    @Override
    public void runOpMode() {
        r.mainArmPower = 0;
        r.init(hardwareMap, false);  //Initialization with safe space for snowflake-shakes.
        r.isAutoWorkAround = true;
        r.clawsPOS = 0.1;  //Claws are set to an extended position
//        r.initClawServosPOS(r.clawsPOS); //"When you try your best but you don't succeed..."
        //FIXME: Can't get r.initClawServosPOS to work, so manually set offsets below. See method for details on not working.
        r.leftBottomClawOffset = 0.1;
        r.rightBottomClawOffset = 1.0;

        //Use these two lines of code below for displaying camera, OR use parameterless line below that for non-displayed camera.
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);

        /*
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
        */

        parameters.vuforiaLicenseKey = g.ourVuforiaLicenseKey;

        /** Indicate which camera on the RC to use. Here we chose the back (HiRes) camera (for
         * greater range), but the front camera might be more convenient. */
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK;
        this.vuforia = ClassFactory.createVuforiaLocalizer(parameters);

        /**Load the data set containing the VuMarks for Relic Recovery. There's only one trackable
         * in this data set: all three of the VuMarks in the game were created from this one template,
         * but differ in their instance id information.
         * @see VuMarkInstanceId */
        VuforiaTrackables relicTrackables = this.vuforia.loadTrackablesFromAsset("RelicVuMark");
        VuforiaTrackable relicTemplate = relicTrackables.get(0);
        relicTemplate.setName("relicVuMarkTemplate"); // can help in debugging; otherwise not necessary

        telemetry.update();
        //Wait for the game to start (driver presses PLAY)
        waitForStart();
        r.runtime.reset();
        r.update();

        relicTrackables.activate();   //Begin looking for and identifying set of VuMarks


        /**
         * See if any of the instances of {@link relicTemplate} are currently visible.
         * {@link RelicRecoveryVuMark} is an enum which can have the following values:
         * UNKNOWN, LEFT, CENTER, and RIGHT. When a VuMark is visible, something other than
         * UNKNOWN will be returned by {@link RelicRecoveryVuMark#from(VuforiaTrackable)}.
         */
        RelicRecoveryVuMark vuMark;
        do {
            vuMark = RelicRecoveryVuMark.from(relicTemplate);
            telemetry.addData("VuMark:", vuMark);
            telemetry.update();
        } while (vuMark == RelicRecoveryVuMark.UNKNOWN);

        double[][] fieldMotions = new double[12][5];
        for (int i = fieldMotions.length - 1; i >= 0; i--) //initializes empty procedure list.
            Arrays.fill(fieldMotions[i], 0.0);

        r.raiseArmSlightly(true);
        r.isAutoWorkAround = false;
        r.update();
        sleep(200);
        r.raiseArmSlightly(false);
        r.update();

        if (vuMark == RelicRecoveryVuMark.LEFT) {
            fieldMotions[0] = fieldTranslate(1,0, r.driveSpeedMin,1500);
            fieldMotions[1] = fieldRotate(true,0.5 * r.driveSpeedMin, 1700);
            fieldMotions[2] = fieldTranslate(0,1,r.driveSpeedMin,500);
        } else if (vuMark == RelicRecoveryVuMark.CENTER) {
            fieldMotions[0] = fieldTranslate(-1,0, r.driveSpeedMin,1500);
            fieldMotions[1] = fieldTranslate(1,0, r.driveSpeedMin,1500);
            fieldMotions[2] = fieldTranslate(0,1, r.driveSpeedMin,1500);
            fieldMotions[3] = fieldTranslate(0,-1, r.driveSpeedMin,1500);
            fieldMotions[4] = fieldRotate(true,0.5 * r.driveSpeedMin, 2000);
            fieldMotions[5] = fieldRotate(false,0.5 * r.driveSpeedMin, 2000);
        } else { //RIGHT mark, by process of elimination.
        }
        for (double[] motion : fieldMotions) { //x,y,acw,cw,speed; time
            //FIXME: Not moving in correct direction. Negating X or Y coordinate causes 90 degree rotation in motion (WTF?)
            r.povDrive(motion[0], motion[1], 0, motion[2], motion[3]); //invert y, because joystick is backwards and povDrive is based on joystick
            r.update();
            sleep((long) motion[4]);
        }
        r.lowerArmSlightly(true);
        r.update();
        sleep(300);
        r.lowerArmSlightly(false);
        r.update();
        sleep(300);

        //Move backwards after cube dropped
        double[] lastCoords = g.rotateCoords(0, -1);
        r.povDrive(lastCoords[0],lastCoords[1],0,0, r.driveSpeedMin);
        r.update();
        sleep(300);
        //It's time to STOP.
        r.povDrive(0,0,0,0,0);
        r.update();

    }
    /**Rotate, mirror, and prepare inputs to be used by POV drive method for translating*/
    double[] fieldTranslate(double x, double y, double speed, long time){ //+y is forward, +x is right
        return g.concat(g.rotateCoords(x, y,3*Math.PI/4), new double[]{0, speed, time});
    }
    /**Rotate prepare inputs to be used by POV drive method for translating, easier for user.*/
    double[] fieldRotate(boolean clockwise, double speed, long time){
        if (!clockwise) speed *= -1;
        return new double[]{0, 0, 1, speed, time};
    }
}