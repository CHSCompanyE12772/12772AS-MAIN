package org.firstinspires.ftc.teamcode.competitioncode;

/**
 * TeleOp that uses Hardware_RWD_RearWheelDrive.
 */

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.Disabled; // Leave this line here even when not used, please

@TeleOp(name="DriveRWD", group="RWD")
//@Disabled         //Enables or disables such OpMode (hide or show on Driver Station OpMode List)

public class DriveRWD extends LinearOpMode {

    Hardware_RWD_RearWheelDrive r = new Hardware_RWD_RearWheelDrive(); //Use the shared hardware and function code.
    General12772 g = new General12772(); //Use the shared general robot code.

    @Override //Does anyone know what this is or what it does?
    public void runOpMode() {
        r.init(hardwareMap, false); //initialization for non-autonomous code. NO SHAKES ALLOWED >:(
        g.init();
        r.clawsPOS = 0.5;  //Claws are set to an extended position
//        r.initClawServosPOS(r.clawsPOS); //"When you try your best but you don't succeed..."
        //Can't get r.initClawServosPOS to work, so manually set offsets below. See method for details on not working.
        r.leftBottomClawOffset = 0.0;
        r.rightBottomClawOffset = 1.0;
        r.leftTopClawOffset = 1.0;
        r.rightTopClawOffset = 0.0;

        // Wait for the game to start (driver presses PLAY)
        waitForStart();
        r.runtime.reset();

        // run until the end of the match (driver presses STOP)
        while (opModeIsActive()) {

            //Control drive motors
            r.setDriveSpeedWithButtons(
                    g.debounce(gamepad1.a,1,8),
                    g.debounce(gamepad1.b,1,7));
            r.povDrive(gamepad1.left_stick_x, gamepad1.left_stick_y, r.driveSpeedStick);

            //Control Arm power and/or position
            r.setArmPositionJoystick(
                    gamepad1.right_stick_y,
                    g.debounce(gamepad1.right_stick_button,1,11),
                    gamepad1.start);

            //Control claw position
            r.setServoPositionTwoButton(gamepad1.left_bumper, gamepad1.right_bumper, gamepad1.left_stick_button);

            //All runtime code in Hardware_RWD_RearWheelDrive
            r.update();

            //BEGIN TELEMETRY SECTION. TELEMETRY WILL NOT WORK IF REFERENCED TO Hardware_RWD_RearWheelDrive.java FOR SOME REASON!
            //I think its because telemetry is provided by TeleOP library, which only OP mode classes can use.
            telemetry.addData("Status",
                    "Run Time: " + r.runtime.toString()
            );
            telemetry.addData("Drive Speed", r.driveSpeedStick);
            telemetry.addData("Motor Power",
                    " leftDrive: " + r.leftDrive.getPower() +
                           " rightDrive: " + r.rightDrive.getPower() +
                           " Arm: " + r.mainArm.getPower()
            );
            telemetry.addData("Servo POS",
                    " clawPOS: " + r.clawsPOS +
                           " leftBottomClaw: " + r.leftBottomClaw.getPosition() +
                           " rightBottomClaw: " + r.rightBottomClaw.getPosition()
            );
            telemetry.addData("ClawOffsets",
                    "left: " + r.leftBottomClawOffset +
                           " right: " + r.rightBottomClawOffset
            );
            telemetry.update();
        }
    }
}