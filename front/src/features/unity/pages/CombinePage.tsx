import React, { useEffect } from 'react';
import { useSelector } from 'react-redux';
import { RootState } from '../../../app/store';
import styled from 'styled-components';
import SideBar from '../../../components/Layout/SideBar';
import CoinFlipPage from '../../coinflip/CoinFlipPage';
import UnityPage from './UnityPage';
import AlgoPage from '../../algorithm/AlgoPage';
import { useDispatch } from 'react-redux';
import { authActions } from '../../auth/authSlice';

const Wrapper = styled.div`
  display: flex;
  flex-direction: row;
  height: 100vh;
`;
const CombinePage = () => {
  const { pageNum } = useSelector((state: RootState) => state.unity);
  const dispatch = useDispatch();
  useEffect(() => {
    dispatch(authActions.fetchUserInfoStart());
    console.log('here');
  }, []);
  return (
    <Wrapper>
      <SideBar />
      {pageNum === 0 && <UnityPage />}
      {pageNum === 3 && <AlgoPage />}
      {pageNum === 4 && <CoinFlipPage />}
    </Wrapper>
  );
};

export default CombinePage;