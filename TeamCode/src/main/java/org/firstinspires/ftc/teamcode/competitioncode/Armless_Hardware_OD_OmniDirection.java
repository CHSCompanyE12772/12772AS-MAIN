package org.firstinspires.ftc.teamcode.competitioncode;

/**
 * Hardware class for 4-Directional robot with wheels at 45 degree angles (and claw arm).
 * Robot will have drive wheel on each side, and can move in x and z directions as well as rotate.
 * Currently Hardware class being used by robot.
 * TODO: Shared code between this class and other claw-robot hardware class.
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

class Armless_Hardware_OD_OmniDirection {

    // Declare OpMode members.
    ElapsedTime runtime = new ElapsedTime();
    DcMotor leftRearDrive = null;
    DcMotor rightFrontDrive = null;
    DcMotor leftFrontDrive = null;
    DcMotor rightRearDrive = null;
    Servo leftTopClaw = null;
    Servo rightTopClaw = null;
    Servo leftBottomClaw = null;
    Servo rightBottomClaw = null;

    //Drive wheel power variables.
    double leftRearDrivePower;
    double rightFrontDrivePower;
    double leftFrontDrivePower;
    double rightRearDrivePower;

    // CLAW MAX AND MIN POS
    double clawPOSMin = 0.0;
    double clawPOSMax = 1.0;
    double clawsPOS = 0;

    //  CLAW OFFSET. used to adjust to real values
    double leftBottomClawOffset = 0.0; //Default ideal values, modified later
    double rightBottomClawOffset = 1.0;
    double leftTopClawOffset = 1.0;
    double rightTopClawOffset = 0.0;

    // DRIVE SPEED
    double driveSpeedMin = 0.25;
    double driveSpeedMed = 0.5;
    double driveSpeedMax = 1.0;
    double driveSpeedStick = driveSpeedMed;

    /* local OpMode members. */
    HardwareMap hwMap           =  null;
    private ElapsedTime period  = new ElapsedTime();

    /* Constructor */
    Armless_Hardware_OD_OmniDirection(){
    }

    //Main function called for initialization stage
    void init(HardwareMap ahwMap, boolean isAuto) {
        // Save reference to Hardware map
        hwMap = ahwMap;

        leftRearDrive = hwMap.get(DcMotor.class, "leftRearDrive");   //LEFT DRIVE WHEEL MOTOR
        rightFrontDrive = hwMap.get(DcMotor.class, "rightFrontDrive");  //RIGHT DRIVE WHEEL MOTOR
        leftFrontDrive = hwMap.get(DcMotor.class, "leftFrontDrive");
        rightRearDrive = hwMap.get(DcMotor.class, "rightRearDrive");

        leftBottomClaw = hwMap.get(Servo.class, "leftBottomClaw");      //LEFT CLAW SERVO
        rightBottomClaw = hwMap.get(Servo.class, "rightBottomClaw");      //RIGHT CLAW SERVO
        leftTopClaw = hwMap.get(Servo.class, "leftTopClaw");      //UPPER LEFT CLAW SERVO
        rightTopClaw = hwMap.get(Servo.class, "rightTopClaw");      //UPPER RIGHT CLAW SERVO

        // Since motors face opposite on each side, one drive motor needs to be reversed.
        // Reverse the motor that runs backwards when connected directly to the battery
        leftRearDrive.setDirection(DcMotor.Direction.FORWARD);
        rightFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightRearDrive.setDirection(DcMotor.Direction.REVERSE);
        // This arm is backwards too, probably.

        // Set all motors to zero power, juuuust in case
        leftRearDrive.setPower(0);
        rightFrontDrive.setPower(0);
        leftFrontDrive.setPower(0);
        rightRearDrive.setPower(0);
        /*
        RELEASE THE SHAKIN'!! Running using encoders causes motors to shake a bit, so best to
        avoid when possible.
        */
        if (isAuto) { //TODO: we could probably make this if-else less repetitive with a polymorphic for loop and array
            leftRearDrive.setMode(DcMotor.RunMode.RESET_ENCODERS);
            rightFrontDrive.setMode(DcMotor.RunMode.RESET_ENCODERS);
            leftFrontDrive.setMode(DcMotor.RunMode.RESET_ENCODERS);
            rightRearDrive.setMode(DcMotor.RunMode.RESET_ENCODERS);

            leftRearDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            leftFrontDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            rightRearDrive.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        else {
            leftRearDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            rightFrontDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            leftFrontDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            rightRearDrive.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }

    //Main function usually called repeatedly after 'Start'
    void update(){
        // Send calculated power to DRIVE MOTORS
        leftRearDrive.setPower(leftRearDrivePower);
        rightFrontDrive.setPower(rightFrontDrivePower);
        leftFrontDrive.setPower(leftFrontDrivePower);
        rightRearDrive.setPower(rightRearDrivePower);
        moveClaw(clawsPOS);
    }

    //used in Autonomous to set speed but retain direction.
    void setDriveSpeed(double speed){ //TODO: could probably make this code less repetitive with objects(?)
        //Left
        if (leftRearDrivePower != 0.0) //avoids divide by zero
            leftRearDrivePower *= speed/Math.abs(leftRearDrivePower); //speed times sign of drivepower
        else
            leftRearDrivePower = speed; //if zero, set to zero.
        //Right
        if (rightFrontDrivePower != 0.0)
            rightFrontDrivePower *= speed/Math.abs(rightFrontDrivePower);
        else
            rightFrontDrivePower = speed;
        //Front
        if (leftFrontDrivePower != 0.0) //avoids divide by zero
            leftFrontDrivePower *= speed/Math.abs(leftFrontDrivePower); //speed times sign of drivepower
        else
            leftFrontDrivePower = speed; //if zero, set to zero.
        //Back
        if (rightRearDrivePower != 0.0)
            rightRearDrivePower *= speed/Math.abs(rightRearDrivePower);
        else
            rightRearDrivePower = speed;
    }

    //set drivePower given single-joystick input
    void povDrive(double x, double y, double cw, double acw, double speed){
        if (cw+acw > 0)
        {
            leftRearDrivePower = Range.scale(cw - acw, -1.0, 1.0, -speed, speed);
            rightFrontDrivePower = -leftRearDrivePower;
            leftFrontDrivePower = leftRearDrivePower;
            rightRearDrivePower = -leftRearDrivePower;
        }
        else
        {
            leftRearDrivePower = Range.scale(y, -1.0, 1.0, -speed, speed);
            rightFrontDrivePower = leftRearDrivePower;
            leftFrontDrivePower = Range.scale(x, -1.0, 1.0, -speed, speed);
            rightRearDrivePower = leftFrontDrivePower;
        }
    }

    void setDriveSpeedWithButtons(boolean increase, boolean decrease){
        //Maybe we should do this with an array? Idk, I don't think it's necessary.
        if (increase) {
            if (driveSpeedStick == driveSpeedMin) driveSpeedStick = driveSpeedMed;
            else if (driveSpeedStick == driveSpeedMed) driveSpeedStick = driveSpeedMax;
            else if (driveSpeedStick == driveSpeedMax) ;
            else driveSpeedStick = driveSpeedMed;
        }
        if (decrease) {
            if (driveSpeedStick == driveSpeedMin) ;
            else if (driveSpeedStick == driveSpeedMed) driveSpeedStick = driveSpeedMin;
            else if (driveSpeedStick == driveSpeedMax) driveSpeedStick = driveSpeedMed;
            else driveSpeedStick = driveSpeedMed;
        }
    }

    void setServoPositionTwoButton(boolean increase, boolean decrease, boolean reset){
        double incr = 0.025; //increment per update. control how fast clawPOS changes.
        if (increase)
            clawsPOS += incr;
        if (decrease)
            clawsPOS -= incr;
        if (reset)
            clawsPOS = clawPOSMin + (clawPOSMax-clawPOSMin)/2 * 1.1;
        // = middle position/2 * 1.1
        clawsPOS = Range.clip(clawsPOS, clawPOSMin, clawPOSMax);
    }

    //Bugged, .getPosition is always returning zero, regardless of actual position. Why??
    //Maybe we could set the zero position on servos physically before starting OpMode? idek...
    //Maybe, but we don't know where zero is given each servo's offset.
    //Perhaps moving the claw during init would help?
    void initClawServosPOS(double startPosition){
        leftBottomClaw.setPosition(0.0);
        rightBottomClaw.setPosition(0.0);
        leftBottomClaw.setPosition(startPosition);
        rightBottomClaw.setPosition(startPosition);
        leftBottomClawOffset =   leftBottomClaw.getPosition() - startPosition;
        rightBottomClawOffset =  rightBottomClaw.getPosition() + startPosition;
    }

    //set positions of TopClaw servos
    void moveClaw(double toPosition){
        leftBottomClaw.setPosition(leftBottomClawOffset + toPosition);
        rightBottomClaw.setPosition(rightBottomClawOffset - toPosition);

        leftTopClaw.setPosition(leftTopClawOffset - toPosition);
        rightTopClaw.setPosition(rightTopClawOffset + toPosition);
    }
}