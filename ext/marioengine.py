from nes_py.wrappers import JoypadSpace
import gym_super_mario_bros
from gym_super_mario_bros.actions import SIMPLE_MOVEMENT, COMPLEX_MOVEMENT
import json

class MarioEngine:

	def run(self, solution, level, render):
		env = gym_super_mario_bros.make(level)
		env = JoypadSpace(env, COMPLEX_MOVEMENT)

		done = True
		reason_finish = "no_more_commands"

		pos = 0
		total_r = 0
		world = 'ola'

		for step in range(len(solution)):
			if done:
				state = env.reset()

			state, reward, done, info = env.step(solution[pos])
			pos+=1

			if world == 'ola':
				world = info['stage']
			#elif world != info['stage']:
			#	reason_finish = "win"
			#	break
			elif info['flag_get'] == True:
				reason_finish = "win"
				break

			if reward == -15: #faleceu
				reason_finish = "death"
				break

			total_r = total_r + reward
			if render == "true":
				env.render()


		env.close()
		return pos, info, reason_finish
