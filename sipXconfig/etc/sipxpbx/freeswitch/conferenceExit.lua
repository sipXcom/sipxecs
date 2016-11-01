fileToPlay = argv[1]
conference = argv[2]
playPrompt = argv[3]
api = freeswitch.API()
if playPrompt == "true" then
  api:executeString("conference "..conference.." play file_string://"..fileToPlay.."!conference/conf-has_left.wav")
end
os.remove(fileToPlay)