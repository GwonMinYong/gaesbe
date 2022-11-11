import { useState } from "react"
import { useSelector } from "react-redux"

import AlgoSelect from "./AlgoSelect"
import AlgoSolve from "./AlgoSolve"

function AlgoAfterStart({ client, ranking, handleLeaveRoom, progress, problemList, problemIndex, inGameUsers, myRank, timeOut }: any) {
  const {InGameInfo} = useSelector((state:any) => state.algo)

  const [passDisabled, setPassDisabled] = useState<boolean>(false)

  const passProblem = () => {
    setPassDisabled(true)
    client.current.send('/api/algo/pass', {}, JSON.stringify({roomCode: InGameInfo.roomCode}))
  }

  return <>
    <button onClick={handleLeaveRoom}>게임방에서 나가기</button>
    {progress === 'select' && <AlgoSelect inGameUsers={inGameUsers} passDisabled={passDisabled} problemList={problemList} passProblem={passProblem} problemIndex={problemIndex} />}
    {progress === 'solve' && <AlgoSolve timeOut={timeOut} myRank={myRank} inGameUsers={inGameUsers} client={client} ranking={ranking} problemList={problemList} problemIndex={problemIndex}  />}
  </>
}
export default AlgoAfterStart