import { Box } from "@mui/material";
import Navbar from "../../components/Navbar";

export default function Dashboard() {
  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        minHeight: "100vh",
        maxWidth: "100%",
        margin: "0 auto",
        padding: "20px",
        backgroundImage: "url(/images/1.png)",
        backgroundSize: "100%", // 覆盖整个容器
        backgroundPosition: "50% 50%", // 居中显示
        backgroundRepeat: "no-repeat", // 不重复
      }}
    >
      <Navbar /> {/* 保留导航条 */}
      {/* 移除所有按钮渲染逻辑 */}
    </Box>
  );
}
