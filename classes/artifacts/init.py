from gpiozero import Button
import RPi.GPIO as GPIO
#button = Button(2)
#GPIO.setmode(GPIO.BCM)
GPIO.setup(2,GPIO.IN)
input = GPIO.input(2)
print(str(input))

#print('iniciado')
#i = 1
#while True:
#	button.wait_for_press()
#	i = i+1
#	print('You pushed me '+ str(i))
