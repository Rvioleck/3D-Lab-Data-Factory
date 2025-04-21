// 导入用户API函数
import {
  createUser,
  updateUser,
  deleteUser,
  getUserById,
  getUserDetailById,
  getUserList as listUsersByPage,
  getAllUsers as listAllUsers
} from './user'

// 导出管理员功能
export {
  createUser,
  updateUser,
  deleteUser,
  getUserById,
  getUserDetailById,
  listUsersByPage,
  listAllUsers
}

// 导出默认对象
export default {
  createUser,
  updateUser,
  deleteUser,
  getUserById,
  getUserDetailById,
  listUsersByPage,
  listAllUsers
}
