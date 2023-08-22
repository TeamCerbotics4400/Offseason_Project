// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.LimelightHelpers;
import frc.robot.Constants.ShooterConstants;
import frc.robot.Constants.VisionConstants;
import frc.robot.subsystems.ArmSubsystem;
import frc.robot.subsystems.FalconShooter;
import frc.robot.subsystems.NodeSelector;
import frc.robot.subsystems.WristSubsystem;
import team4400.StateMachines;
import team4400.StateMachines.IntakeState;

public class StateShooterCommand extends CommandBase {
  /** Creates a new StateIntakeCommand. */
  FalconShooter m_shooter;
  ArmSubsystem m_arm;
  WristSubsystem m_wrist;
  NodeSelector m_selector;
  IntakeState state;

  Timer rumbleTimer = new Timer();

  public StateShooterCommand(FalconShooter m_shooter, ArmSubsystem m_arm, WristSubsystem m_wrist,
  IntakeState state, NodeSelector m_selector) {
    // Use addRequirements() here to declare subsystem dependencies.
    this.m_shooter = m_shooter;
    this.m_wrist = m_wrist;
    this.m_selector = m_selector;
    this.m_arm = m_arm;
    this.state = state;
    addRequirements(m_shooter);
  } 

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
    m_shooter.setCurrentLimit(80, 1.0);
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    String currentIntakeState = StateMachines.currentIntakeState.toString();
    double targetDistance = 
    LimelightHelpers.getTargetPose3d_CameraSpace(VisionConstants.tagLimelightName).getZ();

    if(DriverStation.isAutonomous()){
      if(m_arm.isInPosition() && m_wrist.isInPosition()){
        StateMachines.setIntakeState(state);
        switch(currentIntakeState){
          case "SHOOTING":
            setShooterSetPoints(targetDistance);
          break;  
        }
      } else {
        m_shooter.setMotorsPower(0, 0, 0);
      }
    } else {
      StateMachines.setIntakeState(state);
      switch(currentIntakeState){
        case "SHOOTING":
          setShooterSetPoints(targetDistance);
        break;
      }
    }
    //SmartDashboard.putString("Current Intake State", StateMachines.getIntakeState().toString());
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    m_shooter.setMotorsPower(0, 0, 0);
    StateMachines.setIntakeIdle();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    if(DriverStation.isAutonomous()){
      //Timer.delay(0.5);
      if(m_arm.isInPosition() && onRevs() && StateMachines.isShooting()){
        return true;
      } else {
        return false;
      }
    } 
    return false;
  }

  private void setShooterSetPoints(double targetDistance){
    switch(m_selector.getLevelName()){
      case "Low":
            m_shooter.leftSetpoint(650);
            m_shooter.rightSetpoint(650);
        break;
  
        case "Mid":
            /* En caso de que la interpolacion no jale
            m_shooter.leftSetpoint(1200);
            m_shooter.rightSetpoint(1200);
            m_shooter.horizontalSetpoint(1200);*/
            m_shooter.leftSetpoint(m_shooter.getSpeedForDistanceFalconMid(targetDistance));
            m_shooter.rightSetpoint(m_shooter.getSpeedForDistanceFalconMid(targetDistance));
        break;
  
        case "High":
          /* En caso de que la interpolacion no jale
          m_shooter.leftSetpoint(1350);//2300
          m_shooter.rightSetpoint(1350);//2300
          m_shooter.horizontalSetpoint(3250);//2800*/

          m_shooter.leftSetpoint(m_shooter.getSpeedForDistanceFalconHigh(targetDistance));
          m_shooter.rightSetpoint(m_shooter.getSpeedForDistanceFalconHigh(targetDistance));
        break;
  
        case "Ave Maria":
          m_shooter.leftSetpoint(1200);
          m_shooter.rightSetpoint(1200);
        break;
    }
  }

  public boolean onRevs(){
    double rpmDifference = 0.0;
    switch(m_selector.getLevelName()){
      case "Low":
         rpmDifference = 1850 - m_shooter.getAverageRPM();
      break;

      case "Mid":
         rpmDifference = 1200 - m_shooter.getAverageRPM();
      break;

      case "High":
         rpmDifference = 2050 - m_shooter.getAverageRPM();
      break;

      case "Ave Maria":
         rpmDifference = 1950 - m_shooter.getAverageRPM();
      break;
    }

    return Math.abs(rpmDifference) <= ShooterConstants.shooterTreshold;
    /*if(rpmDifference <= ShooterConstants.shooterTreshold){
      return true;
    } else {
      return false;
    }*/
  }
}
